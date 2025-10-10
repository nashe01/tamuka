package com.kodelink.glide;

public class RideRequest {
    public String rideId;
    public String commuterId;
    public String driverId;
    public LocationData pickupLocation;
    public LocationData destination;
    public String status; // pending, accepted, declined, completed
    public long timestamp;
    public int people;
    public double priceEach;

    public RideRequest() {
        // Default constructor required for Firebase
    }

    public RideRequest(String rideId, String commuterId, String driverId, 
                      LocationData pickupLocation, LocationData destination, String status,
                      int people, double priceEach) {
        this.rideId = rideId;
        this.commuterId = commuterId;
        this.driverId = driverId;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.status = status;
        this.people = people;
        this.priceEach = priceEach;
        this.timestamp = System.currentTimeMillis();
    }

    public static class LocationData {
        public double lat;
        public double lng;
        public String address;

        public LocationData() {
            // Default constructor required for Firebase
        }

        public LocationData(double lat, double lng, String address) {
            this.lat = lat;
            this.lng = lng;
            this.address = address;
        }
    }
}


