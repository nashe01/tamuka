package com.kodelink.glide;

public class RideHistoryItem {
    public String rideId;
    public String pickupLocation;
    public String destinationLocation;
    public String status;
    public long timestamp;
    public int people;
    public double priceEach;
    public String commuterId;
    public String driverId;
    public String userRole; // "driver" or "commuter"

    public RideHistoryItem() {
        // Default constructor required for Firebase
    }

    public RideHistoryItem(String rideId, String pickupLocation, String destinationLocation, 
                          String status, long timestamp, int people, double priceEach,
                          String commuterId, String driverId, String userRole) {
        this.rideId = rideId;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.status = status;
        this.timestamp = timestamp;
        this.people = people;
        this.priceEach = priceEach;
        this.commuterId = commuterId;
        this.driverId = driverId;
        this.userRole = userRole;
    }
}
