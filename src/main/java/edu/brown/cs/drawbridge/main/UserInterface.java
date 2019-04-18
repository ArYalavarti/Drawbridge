package edu.brown.cs.drawbridge.main;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.brown.cs.drawbridge.carpools.Carpools;
import edu.brown.cs.drawbridge.database.MissingDataException;
import edu.brown.cs.drawbridge.models.Trip;
import edu.brown.cs.drawbridge.models.User;
import freemarker.template.Configuration;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract class for the User Interface of the Java project. Contains
 * Handler objects for each end-point defined in the setEnpoints() method.
 *
 * @author Mark Lavrentyev
 */
public final class UserInterface {

  private static final Gson GSON = new Gson();
  private static final String MAPBOX_TOKEN = System.getenv("MAPBOX_KEY");
  private static Carpools carpools;

  /**
   * Default constructor made private since UserInterface is a utility class.
   */
  private UserInterface() {
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out
          .printf("ERROR: Unable use %s for template loading.%n", templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  /**
   * Method to set the database to use when querying.
   *
   * @param dbName
   *     The name of the database.
   *
   * @return true when the set is successful; false when unsuccessful.
   */
  public static boolean setDB(String dbName) {
    try {
      carpools = new Carpools(dbName, System.getenv("DB_USER"),
          System.getenv("DB_PASS"));
      return true;
    } catch (SQLException | ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Instantiates the resources for the analyzers and sets the end-points for
   * the front end GUI.
   */
  public static void setEndpoints() {
    FreeMarkerEngine freeMarker = createEngine();

    Spark.get("/", new HomeGetHandler(), freeMarker);
    Spark.get("/results", new ListGetHandler(), freeMarker);

    Spark.get("/trip/:tid", new DetailGetHandler(), freeMarker);
    Spark.post("/trip/:tid", new DetailPostHandler());

    Spark.get("/my-trips", new UserGetHandler(), freeMarker);
    Spark.post("/my-trips", new UserPostHandler());

    Spark.get("/new", new CreateGetHandler(), freeMarker);
    Spark.post("/new", new CreatePostHandler());

    Spark.get("/help", new InfoGetHandler(), freeMarker);
    Spark.get("/error", new ServerErrorHandler(), freeMarker);

    Spark.get("/*", new Code404Handler(), freeMarker);

    Spark.internalServerError((req, res) -> {
      res.redirect("/error");
      return null;
    });
  }

  // ---------------------------- Home ------------------------------------

  /**
   * Overloaded method to provide an alternate signature for the
   * JSON-processing method.
   *
   * @param uid
   *     The user id for which to create this JSON object.
   * @param tripGroups
   *     a trip-group array.
   *
   * @return A JSON-encodable data structure for trip-groups.
   */
  @SafeVarargs private static List<List<Map<String, String>>> processToJSON(
      String uid, List<Trip>... tripGroups) {

    return processToJSON(uid, Arrays.asList(tripGroups));
  }

  // ---------------------------- List ------------------------------------

  /**
   * Method to help process a list of trip-groups into a JSON-encodable format.
   *
   * @param uid
   *     The user id this is being compiled for.
   * @param tripGroupList
   *     The list of troup-grips.
   *
   * @return A JSON-encodable list of groups of trip objects.
   */
  private static List<List<Map<String, String>>> processToJSON(String uid,
      List<List<Trip>> tripGroupList) {
    List<List<Map<String, String>>> data = new ArrayList<>();
    for (List<Trip> entry : tripGroupList) {

      List<Map<String, String>> innerList = new ArrayList<>();
      for (Trip trip : entry) {
        String status;
        if (trip.getMemberIds().contains(uid)) {
          status = "joined";
        } else if (trip.getHostId().equals(uid)) {
          status = "hosting";
        } else if (trip.getPendingIds().contains(uid)) {
          status = "pending";
        } else {
          status = "join";
        }

        Map<String, String> vals = new HashMap<>();
        vals.put("start", trip.getStartingAddress());
        vals.put("end", trip.getEndingAddress());
        vals.put("date", Long.toString(trip.getDepartureTime()));
        vals.put("currentSize", Integer.toString(trip.getCurrentSize()));
        vals.put("maxSize", Integer.toString(trip.getMaxUsers()));
        if (uid != null) {
          vals.put("costPerPerson", Double.toString(trip.getCostPerUser(uid)));
        } else {
          vals.put("costPerPerson", Double.toString(trip.getCostPerUser("")));
        }
        vals.put("id", Integer.toString(trip.getId()));
        vals.put("name", trip.getName());
        vals.put("status", status);

        innerList.add(vals);
      }
      data.add(innerList);
    }
    return data;
  }

  // --------------------------- Detail -----------------------------------

  /**
   * Handle requests to the home screen of the website.
   */
  private static class HomeGetHandler implements TemplateViewRoute {
    @Override public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("title", "Drawbridge | Home").put("mapboxKey", MAPBOX_TOKEN)
          .put("favicon", "images/favicon.png").build();

      return new ModelAndView(variables, "map.ftl");
    }
  }

  /**
   * Class to handle getting results to display; This handles all requests
   * originating from the home page and from resubmitting the walking time
   * values.
   */
  private static class ListGetHandler implements TemplateViewRoute {
    @Override public ModelAndView handle(Request request, Response response) {
      // Get parameter values
      QueryParamsMap qm = request.queryMap();
      List<List<Map<String, String>>> data;

      try {
        String startName = qm.value("startName");
        String endName = qm.value("endName");

        double startLat = Double.parseDouble(qm.value("startLat"));
        double startLon = Double.parseDouble(qm.value("startLon"));
        double endLat = Double.parseDouble(qm.value("endLat"));
        double endLon = Double.parseDouble(qm.value("endLon"));

        long datetime = Long.parseLong(qm.value("date"));
        String uid = qm.value("userID");

        double walkTime, waitTime;
        if (qm.hasKey("walkTime")) {
          walkTime = qm.get("walkTime").doubleValue();
        } else {
          walkTime = 15 * 60; // 15 minutes walking is the default
        }
        if (qm.hasKey("waitTime")) {
          waitTime = qm.get("waitTime").doubleValue();
        } else {
          waitTime = 30 * 60; // 30 minutes is default for waiting for carpool
        }

        // Do the search
        List<List<Trip>> results = carpools
            .searchWithId(uid, startLat, startLon, endLat, endLon, datetime,
                walkTime, waitTime);
        data = processToJSON(uid, results);

      } catch (NullPointerException e) {
        data = new ArrayList<>();
      }
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("title", "Drawbridge | Results")
          .put("favicon", "images/favicon.png").put("data", GSON.toJson(data))
          .build();

      return new ModelAndView(variables, "results.ftl");
    }
  }

  // ---------------------------- User ------------------------------------

  /**
   * Handler to get information about a specific trip and display it on a page.
   */
  private static class DetailGetHandler implements TemplateViewRoute {
    @Override public ModelAndView handle(Request request, Response response)
        throws SQLException {
      int tid;
      Trip trip;
      List<List<User>> people;
      try {
        tid = Integer.parseInt(request.params(":tid"));
        trip = carpools.getTrip(tid);
        people = carpools.getUsers(tid);
      } catch (NumberFormatException | MissingDataException e) {
        response.status(404);
        Map<String, Object> variables
            = new ImmutableMap.Builder<String, Object>()
            .put("title", String.format("Drawbridge | Not Found"))
            .put("favicon", "images/favicon.png").build();
        return new ModelAndView(variables, "not-found.ftl");
      }

      User host = people.get(0).get(0);
      List<User> members = people.get(1);
      List<User> pending = people.get(2);

      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("title", String.format("Drawbridge | %s", trip.getName()))
          .put("favicon", "images/favicon.png").put("mapboxKey", MAPBOX_TOKEN)
          .put("trip", trip).put("host", host).put("members", members)
          .put("pending", pending).build();
      return new ModelAndView(variables, "detail.ftl");
    }
  }

  /**
   * Handles various actions on the detail page including deleting a trip,
   * joining a trip, approving/denying pending members.
   */
  private static class DetailPostHandler implements Route {
    @Override public Object handle(Request request, Response response) {
      QueryParamsMap qm = request.queryMap();

      int tid;
      try {
        tid = Integer.parseInt(request.params(":tid"));
      } catch (NumberFormatException e) {
        return null;
      }

      String action = qm.value("action");
      String uid = qm.value("user");
      System.out.println(request.body());

      boolean success = false;
      String errorReason = "Command not found";
      try {
        if (action.equals("join")) {
          errorReason = "Database connection failed";
          success = carpools.joinTrip(tid, uid);

        } else if (action.equals("leave")) {
          errorReason = "Database connection failed";
          success = carpools.leaveTrip(tid, uid);

        } else if (action.equals("delete")) {
          errorReason = "Database connection failed";
          success = carpools.deleteTrip(tid, uid);

        } else if (action.equals("approve")) {
          errorReason = "Database connection failed";
          String pendingUID = qm.value("pendingUser");
          success = carpools.approveRequest(tid, uid, pendingUID);

        } else if (action.equals("deny")) {
          errorReason = "Database connection failed";
          String pendingUID = qm.value("pendingUser");
          success = carpools.rejectRequest(tid, uid, pendingUID);
        }
        assert success; // Make sure the db action was completed successfully

      } catch (SQLException | MissingDataException | AssertionError e) {
        JsonObject errObj = new JsonObject();
        errObj.addProperty("error", e.getMessage());
        errObj.addProperty("action", action);
        errObj.addProperty("reason", errorReason);
        return GSON.toJson(errObj);
      }

      response.redirect("/trip/" + tid, 303);
      return null;
    }
  }

  // --------------------------- Create -----------------------------------

  /**
   * Handles the display of the "my trips" page. Simply returns the template.
   */
  private static class UserGetHandler implements TemplateViewRoute {
    @Override public ModelAndView handle(Request request, Response response) {
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("title", "Drawbridge | My Trips")
          .put("favicon", "images/favicon.png").build();

      return new ModelAndView(variables, "my-trips.ftl");
    }
  }

  /**
   * Handles getting the user's trips, split up by category.
   */
  private static class UserPostHandler implements Route {
    @Override public Object handle(Request request, Response response) {
      QueryParamsMap qm = request.queryMap();
      String uid = qm.value("userID");

      // Getting the data
      try {
        List<List<Trip>> userTrips = carpools.getTrips(uid);

        List<Trip> hosting = userTrips.get(0);
        List<Trip> member = userTrips.get(1);
        List<Trip> pending = userTrips.get(2);

        return GSON.toJson(processToJSON(uid, hosting, member, pending));
      } catch (SQLException | MissingDataException e) {
        // TODO: decide stuff to do
        return GSON.toJson(processToJSON(uid));
      }
    }
  }

  // ---------------------------- Info ------------------------------------

  /**
   * Handles loading the "create new trip" page. Simple template serving.
   */
  private static class CreateGetHandler implements TemplateViewRoute {
    @Override public ModelAndView handle(Request request, Response response) {
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("title", "Drawbridge | Create Trip")
          .put("mapboxKey", MAPBOX_TOKEN).put("favicon", "images/favicon.png")
          .build();

      return new ModelAndView(variables, "create.ftl");
    }
  }

  // --------------------------- Errors -----------------------------------

  /**
   * Handles create form submission and actual creation of a new trip.
   */
  private static class CreatePostHandler implements Route {
    @Override public ModelAndView handle(Request request, Response response)
        throws SQLException, MissingDataException {
      QueryParamsMap qm = request.queryMap();

      // Read inputted values from request
      String tripName = qm.value("tripName");
      String startName = qm.value("startName");
      String endName = qm.value("endName");

      double startLat = Double.parseDouble(qm.value("startLat"));
      double startLon = Double.parseDouble(qm.value("startLon"));
      double endLat = Double.parseDouble(qm.value("endLat"));
      double endLon = Double.parseDouble(qm.value("endLon"));

      long departureTime = Long.parseLong(qm.value("date"));
      long eta = (long) (Double.parseDouble(qm.value("eta")) * 60);
      int maxSize = Integer.parseInt(qm.value("size"));
      double totalPrice = Double.parseDouble(qm.value("price"));

      String phone = qm.value("phone");
      String comments = qm.value("comments");
      String method = qm.value("method");

      String hostID = qm.value("userID");

      // Create a new trip through the carpool class
      Trip newTrip = Trip.TripBuilder.newTripBuilder()
          .addIdentification(-1, tripName)
          .addLocations(startLat, startLon, endLat, endLon)
          .addAddressNames(startName, endName).addTimes(departureTime, eta)
          .addDetails(maxSize, totalPrice, phone, method, comments)
          .buildWithUsers(hostID, new ArrayList<>(), new ArrayList<>());

      int tid = carpools.createTrip(newTrip, hostID);

      response.redirect("/trip/" + tid, 303);
      return null;
    }
  }

  /**
   * Class to handle get requests to faq/help/info static page.
   */
  private static class InfoGetHandler implements TemplateViewRoute {
    @Override public ModelAndView handle(Request request, Response response) {
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("title", "Drawbridge | Info")
          .put("favicon", "images/favicon.png").build();

      return new ModelAndView(variables, "info.ftl");
    }
  }

  // --------------------------- Helpers -----------------------------------

  /**
   * Class to handle all page not found requests.
   */
  private static class Code404Handler implements TemplateViewRoute {
    @Override public ModelAndView handle(Request request, Response response) {
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("title", "Drawbridge | Page Not Found")
          .put("favicon", "images/favicon.png").build();

      response.status(404);
      return new ModelAndView(variables, "not-found.ftl");
    }
  }

  /**
   * Class to handle all page not found requests.
   */
  private static class ServerErrorHandler implements TemplateViewRoute {
    @Override public ModelAndView handle(Request request, Response response) {
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("title", "Drawbridge | Page Not Found")
          .put("favicon", "images/favicon.png").build();

      response.status(500);
      return new ModelAndView(variables, "server-error.ftl");
    }
  }
}
