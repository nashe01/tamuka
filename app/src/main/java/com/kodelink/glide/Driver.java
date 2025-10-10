package com.kodelink.glide;

public class Driver {
    public String driverId;
    public String uid; // Firebase Auth UID
    public String name;
    public String gender;
    public LocationData currentLocation;
    public String status; // available, unavailable
    public double rating;
    public int completedRides;

    public Driver() {
        // Default constructor required for Firebase
    }

    public Driver(String driverId, String uid, String name, String gender, LocationData currentLocation, 
                  String status, double rating, int completedRides) {
        this.driverId = driverId;
        this.uid = uid;
        this.name = name;
        this.gender = gender;
        this.currentLocation = currentLocation;
        this.status = status;
        this.rating = rating;
        this.completedRides = completedRides;
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


