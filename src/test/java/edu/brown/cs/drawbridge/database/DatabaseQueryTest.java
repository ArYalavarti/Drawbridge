package edu.brown.cs.drawbridge.database;

public class DatabaseQueryTest {

  //@Test
  public void testDatabaseConnection() {
  try {
  DatabaseQuery test = new DatabaseQuery("//localhost/carpools", "", "");
  } catch (ClassNotFoundException | SQLException e) {
  assert false;
  }
  }

}
