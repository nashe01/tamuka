package com.kodelink.glide;

public class Commuter {
    public String commuterId;
    public String name;
    public String phone;
    public LocationData currentLocation;
    public String ongoingRideId;

    public Commuter() {
        // Default constructor required for Firebase
    }

    public Commuter(String commuterId, String name, String phone, LocationData currentLocation) {
        this.commuterId = commuterId;
        this.name = name;
        this.phone = phone;
        this.currentLocation = currentLocation;
        this.ongoingRideId = null;
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


