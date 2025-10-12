package com.kodelink.glide;

/**
 * Driver - Data model representing a driver in the Swift Ride system
 * 
 * This class contains all the essential information about a driver including:
 * - Personal information (name, gender)
 * - Current location and availability status
 * - Performance metrics (rating, completed rides)
 * - Firebase authentication UID for user management
 * 
 * The class is designed to work seamlessly with Firebase Firestore
 * and Realtime Database for real-time driver tracking.
 * 
 * @author Swift Ride Development Team
 * @version 1.0
 */
public class Driver {
    // Driver identification
    public String driverId;              // Unique driver identifier
    public String uid;                   // Firebase Authentication UID
    
    // Personal information
    public String name;                  // Driver's full name
    public String gender;                // Driver's gender
    
    // Location and status
    public LocationData currentLocation; // Current GPS coordinates and address
    public String status;                // Driver availability: "available" or "unavailable"
    
    // Performance metrics
    public double rating;                // Average rating from passengers (0.0 - 5.0)
    public int completedRides;           // Total number of completed rides

    /**
     * Default constructor required for Firebase serialization
     */
    public Driver() {
        // Default constructor required for Firebase
    }

    /**
     * Constructor to create a new Driver instance with all required fields
     * 
     * @param driverId Unique driver identifier
     * @param uid Firebase Authentication UID
     * @param name Driver's full name
     * @param gender Driver's gender
     * @param currentLocation Current GPS location and address
     * @param status Driver availability status
     * @param rating Average passenger rating
     * @param completedRides Total number of completed rides
     */
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

    /**
     * LocationData - Nested class representing GPS coordinates and address
     * 
     * This class stores the geographical location information for drivers,
     * including latitude, longitude, and human-readable address.
     */
    public static class LocationData {
        public double lat;        // Latitude coordinate
        public double lng;        // Longitude coordinate
        public String address;    // Human-readable address

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


