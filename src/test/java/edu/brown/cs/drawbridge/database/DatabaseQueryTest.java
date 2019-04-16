package edu.brown.cs.drawbridge.database;

import edu.brown.cs.drawbridge.models.Trip;
import edu.brown.cs.drawbridge.models.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DatabaseQueryTest {

  private static final User DUMMY_U1 = new User("1", "one", "one@mail.com");
  private static final User DUMMY_U2 = new User("2", "two", "two@mail.com");
  private static final Trip DUMMY_T1 = Trip.TripBuilder.newTripBuilder()
      .addIdentification(1, "First trip").addLocations(3, 3, 5, 5)
      .addAddressNames("(3, 3)", "(5, 5)").addTimes(1000, 1500)
      .addDetails(5, 20.00, "555-555-5555", "car", "").build();
  private static final Trip DUMMY_T2 = Trip.TripBuilder.newTripBuilder()
      .addIdentification(2, "Second trip").addLocations(0, 0, 5, 4)
      .addAddressNames("(0, 0)", "(5, 4)").addTimes(900, 2000)
      .addDetails(4, 15.50, "444-444-4444", "car", "").build();
  private static final Trip DUMMY_T3 = Trip.TripBuilder.newTripBuilder()
      .addIdentification(3, "Third trip").addLocations(2, 3, 9, 9)
      .addAddressNames("(2, 3)", "(9, 9)").addTimes(500, 1500)
      .addDetails(3, 16.00, "333-333-3333", "car", "").build();
  private static DatabaseQuery test;
  private static int t1, t2, t3;
  private static DatabaseQuery dummyData;

  @BeforeClass public static void oneTimeSetUp()
      throws SQLException, MissingDataException {
    try {
      String username = System.getenv("DB_USER");
      String password = System.getenv("DB_PASS");
      /*
       * Run the following queries in pgadmin:
       * CREATE USER <username> WITH PASSWORD '<password>'
       * GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO <username>
       * GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO <username>
       */
      test = new DatabaseQuery("//127.0.0.1:5432/carpools", username, password);
    } catch (ClassNotFoundException | SQLException e) {
      assert false;
    }
    test.addUser(DUMMY_U1);
    test.addUser(DUMMY_U2);
    t1 = test.createTrip(DUMMY_T1, "1");
    t2 = test.createTrip(DUMMY_T2, "2");
    t3 = test.createTrip(DUMMY_T3, "2");
  }

  @AfterClass public static void oneTimeTearDown() throws SQLException {
    test.clearData();
  }

  @Test public void testAddUser() throws SQLException, MissingDataException {
    test.addUser(DUMMY_U1);
    test.addUser(DUMMY_U2);
    assertNotNull(test.getUserById("1"));
    assertNotNull(test.getUserById("2"));
  }

  @Test public void testCreateTrip() throws SQLException, MissingDataException {
    assertNotNull(test.getTripById(t1));
    assertNotNull(test.getTripById(t2));
    assertNotNull(test.getTripById(t3));
  }

  @Test public void getHostByTripId()
      throws SQLException, MissingDataException {
    assertEquals(test.getHostOnTrip(t1), "1");
    assertEquals(test.getHostOnTrip(t2), "2");
    assertEquals(test.getHostOnTrip(t3), "2");
  }

  @Test public void testDeleteTripManually()
      throws SQLException, MissingDataException {
    Trip t = Trip.TripBuilder.newTripBuilder().addIdentification(4, "extra")
        .addLocations(9, 9, 10, 12).addAddressNames("(9, 9)", "(10, 12)")
        .addTimes(0, 12).addDetails(4, 5.40, "-", "none", "").build();
    int tx = test.createTrip(t, "1");
    assertEquals(test.getHostTripsWithUser("1").size(), 2);
    assertTrue(test.getHostTripsWithUser("1").contains(tx));
    test.deleteTripManually(tx);
    assertEquals(test.getHostTripsWithUser("1").size(), 1);
    assertTrue(test.getHostTripsWithUser("1").contains(t1));
  }

  @Test public void testDeleteTripByTime()
      throws SQLException, MissingDataException {
    //need to test with a trip after current time
    test.deleteExpiredTrips();
    try {
      assertNull(test.getTripById(t1));
    } catch (MissingDataException e) {
      assert true;
    }
    try {
      assertNull(test.getTripById(t2));
    } catch (MissingDataException e) {
      assert true;
    }
    try {
      assertNull(test.getTripById(t3));
    } catch (MissingDataException e) {
      assert true;
    }
    t1 = test.createTrip(DUMMY_T1, "1");
    t2 = test.createTrip(DUMMY_T2, "2");
    t3 = test.createTrip(DUMMY_T3, "2");
    assertNotNull(test.getTripById(t1));
    assertNotNull(test.getTripById(t2));
    assertNotNull(test.getTripById(t3));
  }

  @Test public void testGetRelevantTrips()
      throws SQLException, MissingDataException {
    assertEquals(test.getConnectedTripsWithinTimeRadius(3, 3, 0, 1000, 0),
        new ArrayList<>(Collections.singletonList(test.getTripById(t1))));
    //location but not time
    assertTrue(
        test.getConnectedTripsWithinTimeRadius(3, 3, 0, 2000, 0).isEmpty());
    //time but not location
    assertTrue(
        test.getConnectedTripsWithinTimeRadius(5, 5, 0, 1000, 0).isEmpty());
    //all trips
    assertEquals(test.getConnectedTripsWithinTimeRadius(1, 1, 25, 1500, 1250),
        new ArrayList<>(Arrays
            .asList(test.getTripById(t1), test.getTripById(t2),
                test.getTripById(t3))));
    //selective
    assertEquals(test.getConnectedTripsWithinTimeRadius(3, 3, 2, 750, 300),
        new ArrayList<>(
            Arrays.asList(test.getTripById(t1), test.getTripById(t3))));
  }

  @Test public void testGetConnectedTrips()
      throws SQLException, MissingDataException {
    //not location
    assertTrue(test.getConnectedTripsAfterEta(5, 5, 0, 1000, 0).isEmpty());
    //location but not time
    assertTrue(test.getConnectedTripsAfterEta(3, 3, 0, 2000, 0).isEmpty());
    //before eta within buffer
    assertTrue(test.getConnectedTripsAfterEta(3, 3, 0, 1021, 25).isEmpty());
    //after eta within buffer
    assertEquals(test.getConnectedTripsAfterEta(3, 3, 0, 999, 25),
        new ArrayList<>(Collections.singletonList(test.getTripById(t1))));
    //after eta outside buffer
    assertTrue(test.getConnectedTripsAfterEta(3, 3, 0, 1026, 25).isEmpty());
    //all trips
    assertEquals(test.getConnectedTripsAfterEta(1, 1, 50, -1000000, 2000000),
        new ArrayList<>(Arrays
            .asList(test.getTripById(t1), test.getTripById(t2),
                test.getTripById(t3))));
    //selective
    assertEquals(test.getConnectedTripsAfterEta(1, 1, 10, -112320, 1000000),
        new ArrayList<>(Collections.singletonList(test.getTripById(t2))));
  }

  @Test public void testRequest() throws SQLException {
    test.request(t1, "1");
    test.request(t1, "2");
    assertTrue(test.getRequestsOnTrip(t1).contains("1"));
    assertTrue(test.getRequestsOnTrip(t1).contains("2"));
    assertTrue(test.getRequestTripsWithUser("1").contains(t1));
    assertTrue(test.getRequestTripsWithUser("2").contains(t1));
  }

  @Test public void testApprove() throws SQLException {
    test.request(t3, "1");
    test.request(t3, "2");
    assertTrue(test.getRequestsOnTrip(t3).contains("1"));
    assertTrue(test.getRequestsOnTrip(t3).contains("2"));
    test.approve(t3, "1");
    test.approve(t3, "2");
    assertTrue(test.getMembersOnTrip(t3).contains("1"));
    assertTrue(test.getMembersOnTrip(t3).contains("2"));
    assertTrue(test.getRequestsOnTrip(t3).isEmpty());
  }

  @Test public void testReject() throws SQLException {
    test.request(t2, "1");
    test.request(t2, "2");
    assertTrue(test.getRequestsOnTrip(t2).contains("1"));
    assertTrue(test.getRequestsOnTrip(t2).contains("2"));
    test.reject(t2, "1");
    test.reject(t2, "2");
    assertTrue(test.getMembersOnTrip(t2).isEmpty());
    assertTrue(test.getRequestsOnTrip(t2).isEmpty());
  }

  @Test public void testKick() throws SQLException {
    test.request(t1, "1");
    test.request(t1, "2");
    test.approve(t1, "1");
    test.approve(t1, "2");
    assertTrue(test.getMembersOnTrip(t1).contains("1"));
    assertTrue(test.getMembersOnTrip(t1).contains("2"));
    test.kick(t1, "1");
    assertTrue(test.getMembersOnTrip(t1).contains("2"));
    test.kick(t1, "2");
    assertTrue(test.getMembersOnTrip(t1).isEmpty());
  }

  @Test public void testDummyDataExists()
      throws SQLException, ClassNotFoundException, MissingDataException {
    String username = System.getenv("DB_USER");
    String password = System.getenv("DB_PASS");
    /*
     * Run the following queries in pgadmin:
     * CREATE USER <username> WITH PASSWORD '<password>'
     * GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO <username>
     * GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO <username>
     */
    dummyData = new DatabaseQuery("//127.0.0.1:5432/dummyDatabase", username,
        password);
    dummyData.getUserById("0");
    dummyData.getUserById("1");
    dummyData.getUserById("2");
    dummyData.getUserById("3");
    dummyData.getUserById("4");
    assertNotNull(dummyData.getTripById(1));
    assertNotNull(dummyData.getTripById(2));
    assertNotNull(dummyData.getTripById(3));
    assertNotNull(dummyData.getTripById(4));
    assertNotNull(dummyData.getTripById(5));
    assertEquals(dummyData.getHostOnTrip(1), "0");
    assertEquals(dummyData.getHostOnTrip(2), "1");
    assertEquals(dummyData.getHostOnTrip(3), "3");
    assertEquals(dummyData.getHostOnTrip(4), "0");
    assertEquals(dummyData.getHostOnTrip(5), "2");
    assertTrue(dummyData.getMembersOnTrip(1).contains("3"));
    assertTrue(dummyData.getMembersOnTrip(2).contains("2"));
    assertTrue(dummyData.getMembersOnTrip(2).contains("4"));
    assertTrue(dummyData.getMembersOnTrip(3).contains("4"));
    assertTrue(dummyData.getMembersOnTrip(4).contains("1"));
    assertTrue(dummyData.getMembersOnTrip(5).isEmpty());
    assertTrue(dummyData.getRequestsOnTrip(1).contains("4"));
    assertTrue(dummyData.getRequestsOnTrip(1).contains("2"));
    assertTrue(dummyData.getRequestsOnTrip(1).contains("1"));
    assertTrue(dummyData.getRequestsOnTrip(2).isEmpty());
    assertTrue(dummyData.getRequestsOnTrip(3).contains("1"));
    assertTrue(dummyData.getRequestsOnTrip(4).contains("2"));
    assertTrue(dummyData.getRequestsOnTrip(4).contains("3"));
    assertTrue(dummyData.getRequestsOnTrip(4).contains("4"));
    assertTrue(dummyData.getRequestsOnTrip(5).contains("3"));
  }
}
