package com.kodelink.glide;

public class Vehicle {
    public String vehicleId;
    public String driverId;
    public String vehicleType;
    public String plateNumber;

    public Vehicle() {
        // Default constructor required for Firebase
    }

    public Vehicle(String vehicleId, String driverId, String vehicleType, String plateNumber) {
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.vehicleType = vehicleType;
        this.plateNumber = plateNumber;
    }
}

