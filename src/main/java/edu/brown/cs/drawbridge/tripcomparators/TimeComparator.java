package edu.brown.cs.drawbridge.tripcomparators;

import edu.brown.cs.drawbridge.models.Trip;

import java.util.Comparator;

/**
 * A comparator that compares Trips based its time of departure.
 *
 * @author Jeffrey Zhu
 */
public class TimeComparator implements Comparator<Trip> {

  @Override public int compare(Trip t1, Trip t2) {
    return Integer.compare(t1.getDepartureTime(), t2.getDepartureTime());
  }

}
