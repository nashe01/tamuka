package com.kodelink.glide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;
    private OnRequestRideClickListener requestRideClickListener;
    private LatLng currentLocation;

    public interface OnRequestRideClickListener {
        void onRequestRide(Driver driver);
    }

    public CustomInfoWindowAdapter(Context context, OnRequestRideClickListener listener) {
        this.context = context;
        this.requestRideClickListener = listener;
    }
    
    public void setCurrentLocation(LatLng location) {
        this.currentLocation = location;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null; // Return null to use getInfoContents
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_driver_info, null);
        
        if (marker.getTag() instanceof Driver) {
            Driver driver = (Driver) marker.getTag();
            
            // Populate driver information
            TextView tvDriverName = view.findViewById(R.id.tvDriverName);
            RatingBar ratingBar = view.findViewById(R.id.ratingBar);
            TextView tvCompletedRides = view.findViewById(R.id.tvCompletedRides);
            TextView tvDistance = view.findViewById(R.id.tvDistance);
            Button btnRequestRide = view.findViewById(R.id.btnRequestRide);
            
            tvDriverName.setText(driver.name);
            ratingBar.setRating((float) driver.rating);
            tvCompletedRides.setText(driver.completedRides + " completed rides");
            
            // Calculate and display distance
            if (driver.currentLocation != null && currentLocation != null) {
                double distance = calculateDistance(currentLocation, 
                    new LatLng(driver.currentLocation.lat, driver.currentLocation.lng));
                tvDistance.setText(String.format("%.1f km away", distance));
            } else {
                tvDistance.setText("Distance unavailable");
            }
            
            // Set up Request Ride button click listener
            btnRequestRide.setOnClickListener(v -> {
                android.util.Log.d("CustomInfoWindow", "Button clicked for driver: " + driver.name);
                if (requestRideClickListener != null) {
                    android.util.Log.d("CustomInfoWindow", "Calling onRequestRide");
                    requestRideClickListener.onRequestRide(driver);
                } else {
                    android.util.Log.e("CustomInfoWindow", "requestRideClickListener is null!");
                }
            });
        }
        
        return view;
    }
    
    private double calculateDistance(LatLng point1, LatLng point2) {
        if (point1 == null || point2 == null) return 0;
        
        double lat1 = point1.latitude;
        double lon1 = point1.longitude;
        double lat2 = point2.latitude;
        double lon2 = point2.longitude;
        
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to kilometers
        
        return Math.round(distance * 100.0) / 100.0;
    }
}
