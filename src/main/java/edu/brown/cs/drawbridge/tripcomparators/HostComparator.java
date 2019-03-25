package edu.brown.cs.drawbridge.tripcomparators;

import edu.brown.cs.drawbridge.models.Trip;

/**
 * A comparator that compares Trips based on whether or not it is hosted by the
 * User.
 */
public class HostComparator implements ComparesSearchedTrips {

  private String userId;

  @Override
  public void setId(String userIdentification) {
    this.userId = userIdentification;
  }

  @Override
  public int compare(Trip t1, Trip t2) {
    boolean hostsFirstTrip = t1.getHostId().equals(userId);
    boolean hostsSecondTrip = t2.getHostId().equals(userId);
    if (hostsFirstTrip) {
      if (hostsSecondTrip) {
        return 0;
      } else {
        return -1;
      }
    }
    if (hostsSecondTrip) {
      return 1;
    }
    return 0;
  }
}
