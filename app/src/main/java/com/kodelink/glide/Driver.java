package com.kodelink.glide;

public class Driver {
    public String driverId;
    public String name;
    public String phone;
    public LocationData currentLocation;
    public double rating;
    public int completedRides;
    public String availability; // available, offline
    public String assignedRideId;

    public Driver() {
        // Default constructor required for Firebase
    }

    public Driver(String driverId, String name, String phone, LocationData currentLocation, 
                  double rating, int completedRides, String availability) {
        this.driverId = driverId;
        this.name = name;
        this.phone = phone;
        this.currentLocation = currentLocation;
        this.rating = rating;
        this.completedRides = completedRides;
        this.availability = availability;
        this.assignedRideId = null;
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


