package edu.brown.cs.drawbridge.carpools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import edu.brown.cs.drawbridge.database.DatabaseQuery;
import edu.brown.cs.drawbridge.database.MissingDataException;
import edu.brown.cs.drawbridge.models.Trip;
import edu.brown.cs.drawbridge.tripcomparators.CostComparator;
import edu.brown.cs.drawbridge.tripcomparators.HostComparator;
import edu.brown.cs.drawbridge.tripcomparators.Identifiable;
import edu.brown.cs.drawbridge.tripcomparators.LengthComparator;
import edu.brown.cs.drawbridge.tripcomparators.MemberComparator;
import edu.brown.cs.drawbridge.tripcomparators.MultipleTripComparator;
import edu.brown.cs.drawbridge.tripcomparators.PendingComparator;

/**
 * A class to search for valid paths from a starting to ending location.
 */
public class QuickSearcher {

  // Seconds between switching Trips
  private static final long CONNECTION_WAIT_TIME = 1800;
  private static final int MAX_TRIPS_PER_PATH = 3;
  private static final int MAX_PATH_OPTIONS = 5;
  // Higher multipler means better quality, lower multipler means better
  // quantity; higher multipler reaches best point faster and marks more nodes
  // as visited
  private static final double HEURISTIC_MULTIPLIER = 0.9;

  private static final Comparator<List<Trip>> HOST_COMPARATOR = new HostComparator();
  private static final Comparator<List<Trip>> MEMBER_COMPARATOR = new MemberComparator();
  private static final Comparator<List<Trip>> PENDING_COMPARATOR = new PendingComparator();
  private static final Comparator<List<Trip>> COST_COMPARATOR = new CostComparator();
  private static final Comparator<List<Trip>> LENGTH_COMPARATOR = new LengthComparator();
  private static final List<Identifiable> IDENTIFIABLE = Arrays.asList(
      (Identifiable) HOST_COMPARATOR, (Identifiable) MEMBER_COMPARATOR,
      (Identifiable) PENDING_COMPARATOR, (Identifiable) COST_COMPARATOR);
  private static final Comparator<List<Trip>> TRIP_COMPARATOR = new MultipleTripComparator(
      Arrays.asList(HOST_COMPARATOR, MEMBER_COMPARATOR, PENDING_COMPARATOR,
          LENGTH_COMPARATOR, COST_COMPARATOR));
  private DatabaseQuery database;

  /**
   * Creates a new TripSearcher.
   *
   * @param database
   *          the database object to use for queries
   */
  public QuickSearcher(DatabaseQuery database) {
    this.database = database;
  }

  private void setUser(String userId) {
    for (Identifiable comparator : IDENTIFIABLE) {
      comparator.setUserId(userId);
    }
  }

  /**
   * Calculate the distance (km) from a Trip's ending location to a destination.
   *
   * @param trip
   *          The Trip to calculate destination distance from
   * @param endLat
   *          The latitude of the destination
   * @param endLon
   *          The longitude of the destination
   *
   * @return
   */
  private double distance(Trip trip, double endLat, double endLon) {
    return distance(trip.getEndingLatitude(), endLat, trip.getEndingLongitude(),
        endLon);
  }

  public static double distance(double latitude1, double latitude2,
      double longitude1, double longitude2) {
    double lat1 = Math.toRadians(latitude1);
    double lat2 = Math.toRadians(latitude2);
    double lon1 = Math.toRadians(longitude1);
    double lon2 = Math.toRadians(longitude2);
    final int earthRadius = 6371;
    double latDifference = lat2 - lat1;
    double lonDifference = lon2 - lon1;
    double latSquares = Math.sin(latDifference / 2)
        * Math.sin(latDifference / 2);
    double lonSquares = Math.sin(lonDifference / 2)
        * Math.sin(lonDifference / 2);
    double products = latSquares + lonSquares * Math.cos(lat1) * Math.cos(lat2);
    double radians = 2
        * Math.atan2(Math.sqrt(products), Math.sqrt(1 - products));
    return earthRadius * radians;
  }

  /**
   * Return whether or not a Trip is within the required radius (km) of the
   * destination.
   *
   * @param trip
   *          A Trip that may be within the required radius of the destination
   * @param distanceRadius
   *          The required radius that the Trip must be within of the
   *          destination (km)
   * @param endLat
   *          The latitude of the destination
   * @param endLon
   *          The longitude of the destination
   *
   * @return True if the Trip is within the required radius of the destination.
   *         False otherwise
   */
  private boolean isWithinDestinationRadius(Trip trip, double distanceRadius,
      double endLat, double endLon) {
    return distance(trip, endLat, endLon) <= distanceRadius;
  }

  private List<Trip> unwrap(PathNode node) {
    List<Trip> trips = new ArrayList<Trip>(node.trips);
    trips.add(node.current);
    return trips;
  }

  /**
   * Search for valid paths from a starting location to an ending location.
   *
   * @param userId
   *          The id of the User that is searching
   * @param startLat
   *          The latitude of the starting location
   * @param startLon
   *          The longitude of the starting location
   * @param endLat
   *          The latitude of the ending location
   * @param endLon
   *          The longitude of the ending location
   * @param departureTime
   *          The epoch departure time
   * @param distanceRadius
   *          The maximum walking distance between Trips or between a Trip and
   *          the destination (km)
   * @param timeRadius
   *          The amount of time (seconds) that the Trip can leave within of the
   *          departure time
   *
   * @return A List of valid paths. Each path is a List of Trips.
   */
  public List<List<Trip>> searchWithId(String userId, double startLat,
      double startLon, double endLat, double endLon, long departureTime,
      double distanceRadius, long timeRadius) {
    List<List<Trip>> paths = search(userId, startLat, startLon, endLat, endLon,
        departureTime, distanceRadius, timeRadius);
    Collections.sort(paths, TRIP_COMPARATOR);
    return paths;
  }

  /**
   * Search for valid paths from a starting location to an ending location.
   *
   * @param startLat
   *          The latitude of the starting location
   * @param startLon
   *          The longitude of the starting location
   * @param endLat
   *          The latitude of the ending location
   * @param endLon
   *          The longitude of the ending location
   * @param departureTime
   *          The epoch departure time
   * @param distanceRadius
   *          The maximum walking distance between Trips or between a Trip and
   *          the destination (km)
   * @param timeRadius
   *          The amount of time (seconds) that the Trip can leave within of the
   *          departure time
   *
   * @return A List of valid paths. Each path is a List of Trips.
   */
  public List<List<Trip>> searchWithoutId(double startLat, double startLon,
      double endLat, double endLon, long departureTime, double distanceRadius,
      long timeRadius) {
    List<List<Trip>> paths = search("", startLat, startLon, endLat, endLon,
        departureTime, distanceRadius, timeRadius);
    Collections.sort(paths, COST_COMPARATOR);
    return paths;
  }

  /**
   * Search for valid paths from a starting location to an ending location.
   *
   * @param userId
   *          The id of the User that is searching
   * @param startLat
   *          The latitude of the starting location
   * @param startLon
   *          The longitude of the starting location
   * @param endLat
   *          The latitude of the ending location
   * @param endLon
   *          The longitude of the ending location
   * @param departureTime
   *          The epoch departure time
   * @param distanceRadius
   *          The maximum walking distance between Trips or between a Trip and
   *          the destination (km)
   * @param timeRadius
   *          The amount of time (seconds) that the Trip can leave within of the
   *          departure time
   *
   * @return A List of valid paths. Each path is a List of Trips.
   */
  private List<List<Trip>> search(String userId, double startLat,
      double startLon, double endLat, double endLon, long departureTime,
      double distanceRadius, long timeRadius) {
    // Set the user id for all comparators
    setUser(userId);

    // Set up List of paths found and Queue of nodes to visit
    List<List<Trip>> paths = new ArrayList<>();
    // Contains PathNodes with the distance-to-destination heuristic
    Queue<PathNode> toVisit = new PriorityQueue<>();
    // Contains a Set of visited Trips
    Set<Trip> visited = new HashSet<>();
    // Contains a Map from Trips to their total path weight (no heuristic)
    Map<Trip, Double> weights = new HashMap<>();
    try {
      // Create list of paths from starting location
      List<Trip> startingTrips = database.getConnectedTripsWithinTimeRadius(
          startLat, startLon, distanceRadius, departureTime, timeRadius);
      for (Trip trip : startingTrips) {
        // Add weight to weights HashMap
        weights.put(trip, trip.getTripDistance());
        // Add PathNode with distance-to-destination heuristic to toVisit Queue
        toVisit.add(new PathNode(trip, endLat, endLon, weights));
      }

      // Search for at most 5 valid paths to destination
      while (!toVisit.isEmpty() && paths.size() < MAX_PATH_OPTIONS) {
        PathNode visitingNode = toVisit.poll();
        Trip visitingTrip = visitingNode.current;
        if (isWithinDestinationRadius(visitingTrip, distanceRadius, endLat,
            endLon)) {
          paths.add(unwrap(visitingNode));
          continue;
        }
        if (visited.contains(visitingTrip)) {
          continue;
        } else {
          visited.add(visitingTrip);
        }
        if (visitingNode.trips.size() < MAX_TRIPS_PER_PATH - 1) {
          for (Trip nextTrip : database.getConnectedTripsAfterEta(
              visitingTrip.getEndingLatitude(),
              visitingTrip.getEndingLongitude(), distanceRadius,
              visitingTrip.getEta(), CONNECTION_WAIT_TIME)) {
            if (weights.containsKey(nextTrip)
                && weights.get(nextTrip) < weights.get(visitingTrip)
                    + visitingTrip.distanceTo(nextTrip)) {
              continue;
            } else {
              weights.put(nextTrip, weights.get(visitingTrip)
                  + visitingTrip.distanceTo(nextTrip));
              toVisit.add(new PathNode(visitingNode, nextTrip, weights));
            }
          }
        }
      }
    } catch (SQLException | MissingDataException e) {
      assert false;
    }
    return paths;
  }

  /**
   * A Node of the graph that contains the path to the current Trip and its
   * total path weight.
   */
  private class PathNode implements Comparable<PathNode> {
    private List<Trip> trips; // Previous trips in the path
    private Trip current; // Current trip in the path
    private double totalWeight; // Total weight of the path with the heuristic

    // The ending latitude and longitude of the destination
    private double endLat, endLon;

    /**
     * Construct a new PathNode with a single Trip. Path weight uses the
     * distance heuristic.
     *
     * @param trip
     *          The Trip reached
     * @param endLat
     *          The latitude of the destination
     * @param endLon
     *          The longitude of the destination
     * @param weights
     *          A Map containing information about path weights
     */
    PathNode(Trip trip, double endLat, double endLon,
        Map<Trip, Double> weights) {
      trips = new LinkedList<>();
      current = trip;
      // Actual weight + heuristic
      totalWeight = weights.get(trip)
          + HEURISTIC_MULTIPLIER * distance(trip, endLat, endLon);
      this.endLat = endLat;
      this.endLon = endLon;
    }

    /**
     * Construct a new PathNode by adding a new Trip to an existing PathNode.
     *
     * @param old
     *          The old PathNode
     * @param newTrip
     *          The new Trip reached
     * @param weights
     *          A Map containing information about path weights
     */
    PathNode(PathNode old, Trip newTrip, Map<Trip, Double> weights) {
      trips = new ArrayList<Trip>(old.trips);
      trips.add(old.current);
      current = newTrip;
      endLat = old.endLat;
      endLon = old.endLon;
      // Actual weight + heuristic
      totalWeight = weights.get(newTrip)
          + HEURISTIC_MULTIPLIER * distance(newTrip, endLat, endLon);
    }

    @Override
    public int compareTo(PathNode o) {
      return Double.compare(totalWeight, o.totalWeight);
    }
  }
}
