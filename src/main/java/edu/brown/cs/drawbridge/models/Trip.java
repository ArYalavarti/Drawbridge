package edu.brown.cs.drawbridge.models;

import java.util.List;

/**
 * This class models a carpooling Trip.
 */
public class Trip {

  private int id;
  private String name;
  private double startingLatitude, startingLongitude, endingLatitude,
      endingLongitude;
  private int departureTime, eta;
  private int maxUsers;
  private double cost;
  private String phoneNumber, methodOfTransportation, comments;
  private String hostId;
  private List<String> memberIds, pendingIds;

  /**
   * Set the id and name of the Trip.
   *
   * @param tripId
   *          The id of the Trip
   * @param tripName
   *          The name of the Trip
   */
  private void setIdentification(int tripId, String tripName) {
    this.id = tripId;
    this.name = tripName;
  }

  /**
   * Set the starting and ending locations of the Trip.
   *
   * @param startLat
   *          The starting latitude
   * @param startLon
   *          The starting longitude
   * @param endLat
   *          The ending latitude
   * @param endLon
   *          The ending longitude
   */
  private void setLocations(double startLat, double startLon, double endLat,
      double endLon) {
    this.startingLatitude = startLat;
    this.startingLongitude = startLon;
    this.endingLatitude = endLat;
    this.endingLongitude = endLon;
  }

  /**
   * Set the start and end times of the Trip.
   *
   * @param startTime
   *          The epoch departure time
   * @param endTime
   *          The epoch estimated time of arrival
   */
  private void setTimes(int startTime, int endTime) {
    this.departureTime = startTime;
    this.eta = endTime;
  }

  /**
   * Set the host phone number, method of transportation, and comments of the
   * Trip.
   *
   * @param hostPhone
   *          The host's phone number
   * @param transportation
   *          The method of transportation
   * @param tripComments
   *          The Trip comments
   */
  private void setDetails(int maxSize, double tripCost, String hostPhone,
      String transportation, String tripComments) {
    this.maxUsers = maxSize;
    this.cost = tripCost;
    this.phoneNumber = hostPhone;
    this.methodOfTransportation = transportation;
    this.comments = tripComments;
  }

  /**
   * Set the host id, the ids of members in the Trip, and the ids of Users who
   * have requested to join the Trip.
   *
   * @param host
   *          The id of the host
   * @param members
   *          The ids of members in the Trip
   * @param pending
   *          The ids of Users who have requested to join the Trip
   */
  private void setUsers(String host, List<String> members,
      List<String> pending) {
    this.hostId = host;
    this.memberIds = members;
    this.pendingIds = pending;
  }

  /**
   * Get the id of the Trip.
   *
   * @return The Trip's id
   */
  public int getId() {
    return id;
  }

  /**
   * Get the name of the Trip.
   *
   * @return The Trip's name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the starting latitude of the Trip.
   *
   * @return The starting latitude of the Trip
   */
  public double getStartingLatitude() {
    return startingLatitude;
  }

  /**
   * Get the starting longitude of the Trip.
   *
   * @return The starting longitude of the Trip
   */
  public double getStartingLongitude() {
    return startingLongitude;
  }

  /**
   * Get the ending latitude of the Trip.
   *
   * @return The ending latitude of the Trip
   */
  public double getEndingLatitude() {
    return endingLatitude;
  }

  /**
   * Get the ending longitude of the Trip.
   *
   * @return The ending longitude of the Trip
   */
  public double getEndingLongitude() {
    return endingLongitude;
  }

  /**
   * Get the departure time of the Trip.
   *
   * @return The departure time of the Trip
   */
  public int getDepartureTime() {
    return departureTime;
  }

  /**
   * Get the eta of the Trip.
   *
   * @return The eta of the Trip
   */
  public int getEta() {
    return eta;
  }

  /**
   * Get the maximum number of Users.
   *
   * @return The maximum number of Users
   */
  public int getMaxUsers() {
    return maxUsers;
  }

  /**
   * Get the cost of the Trip.
   *
   * @return The cost of the Trip
   */
  public double getCost() {
    return cost;
  }

  /**
   * Get the host's phone number.
   *
   * @return The host's phone number
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * Get the method of transportation.
   *
   * @return The method of transportation
   */
  public String getMethodOfTransportation() {
    return methodOfTransportation;
  }

  /**
   * Get the Trip comments.
   *
   * @return The Trip comments
   */
  public String getComments() {
    return comments;
  }

  /**
   * Get the id of the Trip's host.
   *
   * @return The id of the Trip's host
   * @throws IllegalArgumentException
   *           If User information has not been initialized
   */
  public String getHostId() {
    if (hostId == null) {
      throw new IllegalArgumentException(
          "ERROR: User information has not been initialized.");
    }
    return hostId;
  }

  /**
   * Get the ids of members in the Trip.
   *
   * @return A List of ids of members in the Trip
   * @throws IllegalArgumentException
   *           If User information has not been initialized
   */
  public List<String> getMemberIds() {
    if (memberIds == null) {
      throw new IllegalArgumentException(
          "ERROR: User information has not been initialized.");
    }
    return memberIds;
  }

  /**
   * Get the ids of Users who have requested to join the Trip.
   *
   * @return A list of Users who have requested to join the Trip
   * @throws IllegalArgumentException
   *           If User information has not been initialized
   */
  public List<String> getPendingIds() {
    if (pendingIds == null) {
      throw new IllegalArgumentException(
          "ERROR: User information has not been initialized.");
    }
    return pendingIds;
  }

  /**
   * Get the current number of Users in the Trip.
   *
   * @return The current number of Users in the Trip
   * @throws IllegalArgumentException
   *           If User information has not been initialized
   */
  public int getCurrentSize() {
    if (memberIds == null) {
      throw new IllegalArgumentException(
          "ERROR: User information has not been initialized.");
    }
    return 1 + memberIds.size();
  }

  /**
   * Get the cost per User in the Trip.
   *
   * @param userId
   *          The id of a User
   * @return The cost per existing User in the Trip if the given User is already
   *         in the Trip. Otherwise, the cost per existing User plus the current
   *         User
   * @throws IllegalArgumentException
   *           If User information has not been initialized
   */
  public double getCostPerUser(String userId) {
    int currentSize = getCurrentSize();
    if (userId.equals(hostId) || memberIds.contains(userId)) {
      // If the user is in the group, return cost / number of Users in the Trip
      return cost / currentSize;
    } else {
      // Otherwise, return cost / (number of Users in the Trip + 1)
      return cost / (currentSize + 1);
    }
  }

  /**
   * A class used to build a Trip.
   */
  public static class TripBuilder {

    /**
     * Create a new IdentificationStep object.
     *
     * @return A new TripSteps object as an IdentificationStep
     */
    public static IdentificationStep newTripBuilder() {
      return new TripSteps();
    }

    /**
     * An interface that adds identification and produces a LocationStep.
     */
    public interface IdentificationStep {
      /**
       * Add the id and the name of the Trip.
       *
       * @param id
       *          The id of the Trip
       * @param name
       *          The name of the Trip
       * @return A LocationStep containing the new identification data
       */
      LocationStep addIdentification(int id, String name);
    }

    /**
     * An interface that adds starting and ending locations and produces a
     * TimeStep.
     */
    public interface LocationStep {
      /**
       * Add the starting and ending locations of the Trip.
       *
       * @param startingLatitude
       *          The starting latitude of the Trip
       * @param startingLongitude
       *          The starting longitude of the Trip
       * @param endingLatitude
       *          The ending latitude of the Trip
       * @param endingLongitude
       *          The ending longitude of the Trip
       * @return A TimeStep containing the new location data
       */
      TimeStep addLocations(double startingLatitude, double startingLongitude,
          double endingLatitude, double endingLongitude);
    }

    /**
     * An interface that adds departure and arrival time and produces a
     * DetailsStep.
     */
    public interface TimeStep {
      /**
       * Add the departure and arrival time of the Trip.
       *
       * @param departuretime
       *          The epoch departure time of the Trip
       * @param eta
       *          The epoch estimated time of arrival of the Trip
       * @return A DetailsStep containing the new time data
       */
      DetailsStep addTimes(int departuretime, int eta);
    }

    /**
     * An interface that adds Trip details and produces a BuildStep.
     */
    public interface DetailsStep {
      /**
       * Add the trip details to the object.
       *
       * @param maxUsers
       *          The maximum number of Users allowed in the Trip
       * @param cost
       *          The cost of the Trip
       * @param phoneNumber
       *          The host's phone number
       * @param methodOfTransportation
       *          The method of transportation
       * @param comments
       *          An optional description of the Trip
       * @return A BuildStep containing the new Trip details data
       */
      BuildStep addDetails(int maxUsers, double cost, String phoneNumber,
          String methodOfTransportation, String comments);
    }

    /**
     * A class that builds a Trip.
     */
    public interface BuildStep {
      /**
       * Build a Trip using data in the BuildStep.
       *
       * @return A new Trip
       */
      Trip build();

      /**
       * Build a Trip given the id of the host, ids of members, and ids of Users
       * who have requested to join the Trip.
       *
       * @param hostId
       *          The id of the host
       * @param memberids
       *          The ids of members in the Trip
       * @param pendingIds
       *          The ids of Users who have requested to join the Trip
       * @return A new Trip
       */
      Trip buildWithUsers(String hostId, List<String> memberids,
          List<String> pendingIds);
    }

    /**
     * A class that contains data about a Trip. It creates a Trip by
     * incrementally adding data about the Trip.
     */
    private static class TripSteps implements IdentificationStep, LocationStep,
        TimeStep, DetailsStep, BuildStep {
      // Identification
      private int id;
      private String name;
      // Location
      private double startingLatitude, startingLongitude, endingLatitude,
          endingLongitude;
      // Time
      private int departureTime, eta;
      // Details
      private int maxUsers;
      private double cost;
      private String phoneNumber, methodOfTransportation, comments;

      @Override
      public LocationStep addIdentification(int tripId, String tripName) {
        this.id = tripId;
        this.name = tripName;
        return this;
      }

      @Override
      public TimeStep addLocations(double startLat, double startLon,
          double endLat, double endLon) {
        this.startingLatitude = startLat;
        this.startingLongitude = startLon;
        this.endingLatitude = endLat;
        this.endingLongitude = endLon;
        return this;
      }

      @Override
      public DetailsStep addTimes(int startTime, int endTime) {
        this.departureTime = startTime;
        this.eta = endTime;
        return this;
      }

      @Override
      public BuildStep addDetails(int maxSize, double costOfTrip,
          String hostPhone, String transportation, String tripComments) {
        this.maxUsers = maxSize;
        this.cost = costOfTrip;
        this.phoneNumber = hostPhone;
        this.methodOfTransportation = transportation;
        this.comments = tripComments;
        return this;
      }

      @Override
      public Trip build() {
        Trip trip = new Trip();
        trip.setIdentification(id, name);
        trip.setLocations(startingLatitude, startingLongitude, endingLatitude,
            endingLongitude);
        trip.setTimes(departureTime, eta);
        trip.setDetails(maxUsers, cost, phoneNumber, methodOfTransportation,
            comments);
        return trip;
      }

      @Override
      public Trip buildWithUsers(String host, List<String> members,
          List<String> pending) {
        Trip trip = new Trip();
        trip.setIdentification(id, name);
        trip.setLocations(startingLatitude, startingLongitude, endingLatitude,
            endingLongitude);
        trip.setTimes(departureTime, eta);
        trip.setDetails(maxUsers, cost, phoneNumber, methodOfTransportation,
            comments);
        trip.setUsers(host, members, pending);
        return trip;
      }
    }
  }
}
