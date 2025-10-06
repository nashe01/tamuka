package com.kodelink.glide;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataInitializer {
    private static final String TAG = "DataInitializer";
    
    public static void initializeSampleData(Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://glide-77761-default-rtdb.firebaseio.com/");
        DatabaseReference databaseRef = database.getReference();
        
        // Initialize sample drivers
        initializeSampleDrivers(databaseRef);
        
        // Initialize sample commuters
        initializeSampleCommuters(databaseRef);
        
        Log.d(TAG, "Sample data initialized");
    }
    
    private static void initializeSampleDrivers(DatabaseReference databaseRef) {
        // Sample Driver 1
        Driver driver1 = new Driver(
                "driver_001",
                "John Doe",
                "+263771234567",
                new Driver.LocationData(-17.82486, 31.05343, "Harare CBD"),
                4.9,
                34,
                "available"
        );
        
        // Sample Driver 2
        Driver driver2 = new Driver(
                "driver_002",
                "Jane Smith",
                "+263771234568",
                new Driver.LocationData(-17.82765, 31.05612, "Eastlea"),
                4.7,
                28,
                "available"
        );
        
        // Sample Driver 3
        Driver driver3 = new Driver(
                "driver_003",
                "Mike Johnson",
                "+263771234569",
                new Driver.LocationData(-17.82000, 31.05000, "Avondale"),
                4.8,
                42,
                "offline"
        );
        
        // Save drivers to Firebase
        databaseRef.child("drivers").child("driver_001").setValue(driver1);
        databaseRef.child("drivers").child("driver_002").setValue(driver2);
        databaseRef.child("drivers").child("driver_003").setValue(driver3);
    }
    
    private static void initializeSampleCommuters(DatabaseReference databaseRef) {
        // Sample Commuter 1
        Commuter commuter1 = new Commuter(
                "commuter_001",
                "Blessing",
                "+263771234560",
                new Commuter.LocationData(-17.82486, 31.05343, "Harare CBD")
        );
        
        // Sample Commuter 2
        Commuter commuter2 = new Commuter(
                "commuter_002",
                "Sarah",
                "+263771234561",
                new Commuter.LocationData(-17.82765, 31.05612, "Eastlea")
        );
        
        // Save commuters to Firebase
        databaseRef.child("commuters").child("commuter_001").setValue(commuter1);
        databaseRef.child("commuters").child("commuter_002").setValue(commuter2);
    }
}


