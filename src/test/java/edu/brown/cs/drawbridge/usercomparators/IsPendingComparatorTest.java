package edu.brown.cs.drawbridge.usercomparators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import edu.brown.cs.drawbridge.models.Trip;
import edu.brown.cs.drawbridge.models.User;

/**
 * Tests IsPendingComparator constructor and methods.
 */
public class IsPendingComparatorTest {

  /**
   * Test IsPendingComparator constructor.
   */
  @Test
  public void testConstructor() {
    List<String> memberIds = new LinkedList<String>();
    memberIds.add("1");
    List<String> pendingIds = new LinkedList<String>();
    pendingIds.add("2");
    Trip trip = Trip.TripBuilder.newTripBuilder().addIdentification(0, "name")
        .addLocations(0, 0, 0, 0).addTimes(10, 20)
        .addDetails(5, 100, "1d234567890", "My car", "comments")
        .buildWithUsers("0", memberIds, pendingIds);
    ComparesUsersInTrip isPendingComparator = new IsPendingComparator();
    isPendingComparator.setTrip(trip);
    assertNotNull(isPendingComparator);
  }

  /**
   * Test compare method given neither User is pending the Trip.
   */
  @Test
  public void testNeitherPending() {
    List<String> memberIds = new LinkedList<String>();
    memberIds.add("1");
    List<String> pendingIds = new LinkedList<String>();
    pendingIds.add("2");
    Trip trip = Trip.TripBuilder.newTripBuilder().addIdentification(0, "name")
        .addLocations(0, 0, 0, 0).addTimes(10, 20)
        .addDetails(5, 100, "1d234567890", "My car", "comments")
        .buildWithUsers("0", memberIds, pendingIds);

    User user1 = new User("10", "name1", "email");
    User user2 = new User("11", "name1", "email");
    ComparesUsersInTrip isPendingComparator = new IsPendingComparator();
    isPendingComparator.setTrip(trip);
    assertEquals(isPendingComparator.compare(user1, user2), 0);
  }

  /**
   * Test compare method given the first User is pending the Trip.
   */
  @Test
  public void testFirstPending() {
    List<String> memberIds = new LinkedList<String>();
    memberIds.add("1");
    List<String> pendingIds = new LinkedList<String>();
    pendingIds.add("2");
    Trip trip = Trip.TripBuilder.newTripBuilder().addIdentification(0, "name")
        .addLocations(0, 0, 0, 0).addTimes(10, 20)
        .addDetails(5, 100, "1d234567890", "My car", "comments")
        .buildWithUsers("0", memberIds, pendingIds);

    User user1 = new User("2", "name1", "email");
    User user2 = new User("11", "name1", "email");
    ComparesUsersInTrip isPendingComparator = new IsPendingComparator();
    isPendingComparator.setTrip(trip);
    assertEquals(isPendingComparator.compare(user1, user2), -1);
  }

  /**
   * Test compare method given the second User is pending the Trip.
   */
  @Test
  public void testSecondMember() {
    List<String> memberIds = new LinkedList<String>();
    memberIds.add("1");
    List<String> pendingIds = new LinkedList<String>();
    pendingIds.add("2");
    Trip trip = Trip.TripBuilder.newTripBuilder().addIdentification(0, "name")
        .addLocations(0, 0, 0, 0).addTimes(10, 20)
        .addDetails(5, 100, "1d234567890", "My car", "comments")
        .buildWithUsers("0", memberIds, pendingIds);

    User user1 = new User("10", "name1", "email");
    User user2 = new User("2", "name1", "email");
    ComparesUsersInTrip isPendingComparator = new IsPendingComparator();
    isPendingComparator.setTrip(trip);
    assertEquals(isPendingComparator.compare(user1, user2), 1);
  }

  /**
   * Test IsPendingComparator by sorting multiple Users.
   */
  @Test
  public void testMultipleUsers() {
    List<String> memberIds = new LinkedList<String>();
    memberIds.add("1");
    List<String> pendingIds = new LinkedList<String>();
    pendingIds.add("2");
    pendingIds.add("3");
    Trip trip = Trip.TripBuilder.newTripBuilder().addIdentification(0, "name")
        .addLocations(0, 0, 0, 0).addTimes(10, 20)
        .addDetails(5, 100, "1d234567890", "My car", "comments")
        .buildWithUsers("0", memberIds, pendingIds);

    User user1 = new User("10", "name1", "email");
    User user2 = new User("2", "name1", "email");
    User user3 = new User("3", "name1", "email");

    ArrayList<User> users = new ArrayList<User>();
    users.add(user1);
    users.add(user2);
    users.add(user3);
    ArrayList<User> expected = new ArrayList<User>();
    expected.add(user2);
    expected.add(user3);
    expected.add(user1);

    ComparesUsersInTrip isPendingComparator = new IsPendingComparator();
    isPendingComparator.setTrip(trip);
    Collections.sort(users, isPendingComparator);
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(users.get(i), expected.get(i));
    }
  }

}
