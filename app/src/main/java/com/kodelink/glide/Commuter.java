package com.kodelink.glide;

public class Commuter {
    public String commuterId;
    public String uid; // Firebase Auth UID
    public String name;
    public LocationData currentLocation;

    public Commuter() {
        // Default constructor required for Firebase
    }

    public Commuter(String commuterId, String uid, String name, LocationData currentLocation) {
        this.commuterId = commuterId;
        this.uid = uid;
        this.name = name;
        this.currentLocation = currentLocation;
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


