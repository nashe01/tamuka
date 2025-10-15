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
    
    // Enhanced fields for better display
    public String commuterName;
    public String driverName;
    public String commuterPhone;
    public String driverPhone;
    public String totalPrice;
    public String duration; // ride duration if completed
    public String distance; // ride distance if available

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
    
    // Helper method to get display name for the other party
    public String getOtherPartyName() {
        if (userRole.equals("driver")) {
            return commuterName != null ? commuterName : "Rider: " + commuterId;
        } else {
            return driverName != null ? driverName : "Driver: " + driverId;
        }
    }
    
    // Helper method to get status display text
    public String getStatusDisplayText() {
        switch (status.toLowerCase()) {
            case "completed":
                return "COMPLETED";
            case "declined":
                return "DECLINED";
            case "cancelled":
                return "CANCELLED";
            case "timeout":
                return "TIMED OUT";
            case "pending":
                return "PENDING";
            case "accepted":
                return "ACCEPTED";
            default:
                return status.toUpperCase();
        }
    }
}
