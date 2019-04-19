package edu.brown.cs.drawbridge.tripcomparators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import edu.brown.cs.drawbridge.models.Trip;

/**
 * Tests PendingComparator constructor and methods.
 */
public class PendingComparatorTest {

  /**
   * Test PendingComparator constructor.
   */
  @Test
  public void testConstructor() {
    assertNotNull(new PendingComparator());
  }

  /**
   * Test compare method given two paths with equal number of Trip with User as
   * pending.
   */
  @Test
  public void testPendingEqual() {
    List<String> pendingIds = new LinkedList<String>();
    pendingIds.add("10");
    Trip trip1 = Trip.TripBuilder.newTripBuilder().addIdentification(1, "name1")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    Trip trip2 = Trip.TripBuilder.newTripBuilder().addIdentification(2, "name2")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    Trip trip3 = Trip.TripBuilder.newTripBuilder().addIdentification(3, "name3")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    Trip trip4 = Trip.TripBuilder.newTripBuilder().addIdentification(4, "name4")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    List<Trip> path1 = new ArrayList<Trip>(Arrays.asList(trip1, trip2));
    List<Trip> path2 = new ArrayList<Trip>(Arrays.asList(trip3, trip4));

    PendingComparator pendingComparator = new PendingComparator();
    pendingComparator.setUserId("10");
    assertEquals(pendingComparator.compare(path1, path2), 0);
  }

  /**
   * Test compare method given the first path has User as pending more
   * frequently.
   */
  @Test
  public void testFirstPendingMore() {
    List<String> pendingIds = new LinkedList<String>();
    pendingIds.add("10");
    Trip trip1 = Trip.TripBuilder.newTripBuilder().addIdentification(1, "name1")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    Trip trip2 = Trip.TripBuilder.newTripBuilder().addIdentification(2, "name2")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    Trip trip3 = Trip.TripBuilder.newTripBuilder().addIdentification(3, "name3")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    Trip trip4 = Trip.TripBuilder.newTripBuilder().addIdentification(4, "name4")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(),
            new LinkedList<String>());
    List<Trip> path1 = new ArrayList<Trip>(Arrays.asList(trip1, trip2));
    List<Trip> path2 = new ArrayList<Trip>(Arrays.asList(trip3, trip4));

    PendingComparator pendingComparator = new PendingComparator();
    pendingComparator.setUserId("10");
    assertEquals(pendingComparator.compare(path1, path2), -1);
  }

  /**
   * Test compare method given the second path has User as pending more
   * frequently.
   */
  @Test
  public void testSecondPendingMore() {
    List<String> pendingIds = new LinkedList<String>();
    pendingIds.add("10");
    Trip trip1 = Trip.TripBuilder.newTripBuilder().addIdentification(1, "name1")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(),
            new LinkedList<String>());
    Trip trip2 = Trip.TripBuilder.newTripBuilder().addIdentification(2, "name2")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    Trip trip3 = Trip.TripBuilder.newTripBuilder().addIdentification(3, "name3")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    Trip trip4 = Trip.TripBuilder.newTripBuilder().addIdentification(4, "name4")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    List<Trip> path1 = new ArrayList<Trip>(Arrays.asList(trip1, trip2));
    List<Trip> path2 = new ArrayList<Trip>(Arrays.asList(trip3, trip4));

    PendingComparator pendingComparator = new PendingComparator();
    pendingComparator.setUserId("10");
    assertEquals(pendingComparator.compare(path1, path2), 1);
  }

  /**
   * Test PendingComparator by sorting multiple paths.
   */
  @Test
  public void testMultiplePaths() {
    List<String> pendingIds = new LinkedList<String>();
    pendingIds.add("10");
    Trip trip1 = Trip.TripBuilder.newTripBuilder().addIdentification(1, "name1")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(),
            new LinkedList<String>());
    Trip trip2 = Trip.TripBuilder.newTripBuilder().addIdentification(2, "name2")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(),
            new LinkedList<String>());
    Trip trip3 = Trip.TripBuilder.newTripBuilder().addIdentification(3, "name3")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(), pendingIds);
    Trip trip4 = Trip.TripBuilder.newTripBuilder().addIdentification(4, "name4")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 100, "1234567890", "My car", "comments")
        .buildWithUsers("0", new LinkedList<String>(),
            new LinkedList<String>());
    Trip trip5 = Trip.TripBuilder.newTripBuilder().addIdentification(5, "name5")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 70, "1234567890", "My car", "comments")
        .buildWithUsers("1", new LinkedList<String>(), pendingIds);
    Trip trip6 = Trip.TripBuilder.newTripBuilder().addIdentification(6, "name6")
        .addLocations(0, 0, 0, 0).addAddressNames("start", "end")
        .addTimes(10, 20).addDetails(5, 30, "1234567890", "My car", "comments")
        .buildWithUsers("1", new LinkedList<String>(), pendingIds);

    List<Trip> path1 = new ArrayList<Trip>(Arrays.asList(trip1, trip2));
    List<Trip> path2 = new ArrayList<Trip>(Arrays.asList(trip3, trip4));
    List<Trip> path3 = new ArrayList<Trip>(Arrays.asList(trip5, trip6));

    List<List<Trip>> paths = new ArrayList<List<Trip>>();
    paths.add(path1);
    paths.add(path2);
    paths.add(path3);
    List<List<Trip>> expected = new ArrayList<List<Trip>>();
    expected.add(path3);
    expected.add(path2);
    expected.add(path1);

    PendingComparator pendingComparator = new PendingComparator();
    pendingComparator.setUserId("10");
    Collections.sort(paths, pendingComparator);
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(paths.get(i), expected.get(i));
    }
  }
}
