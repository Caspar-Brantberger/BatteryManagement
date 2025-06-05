package com.example.BatteryManagement;

import java.time.LocalDateTime;

public class BatteryStatus {

    private double currentPercentage;

    private double currentPowerKw;

    private LocalDateTime timestamp;

    public BatteryStatus(){
    }
    public BatteryStatus(double currentPercentage, double currentPowerKw, LocalDateTime timestamp) {
        this.currentPercentage = currentPercentage;
        this.currentPowerKw = currentPowerKw;
        this.timestamp = timestamp;
    }
    public double getCurrentPercentage() {
        return currentPercentage;
    }
    public void setCurrentPercentage(double currentPercentage) {
        this.currentPercentage = currentPercentage;
    }
    public double getCurrentPowerKw() {
        return currentPowerKw;
    }
    public void setCurrentPowerKw(double currentPowerKw) {
        this.currentPowerKw = currentPowerKw;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BatteryStatus{" +
                "currentPercentage=" + currentPercentage +
                ", currentPowerKw=" + currentPowerKw +
                ", timestamp=" + timestamp +
                '}';
    }
}
