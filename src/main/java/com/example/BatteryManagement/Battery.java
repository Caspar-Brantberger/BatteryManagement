package com.example.BatteryManagement;

import java.time.LocalDateTime;

public class Battery {

    private String PriceArea;

    private double pricePerKwh;

    private LocalDateTime timestamp;

    public Battery() {
    }

    public Battery(String priceArea, double pricePerKwh, LocalDateTime timestamp) {
        PriceArea = priceArea;
        this.pricePerKwh = pricePerKwh;
        this.timestamp = timestamp;
    }
    public String getPriceArea() {
        return PriceArea;
    }
    public void setPriceArea(String priceArea) {
        PriceArea = priceArea;
    }
    public double getPricePerKwh() {
        return pricePerKwh;
    }
    public void setPricePerKwh(double pricePerKwh) {
        this.pricePerKwh = pricePerKwh;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Battery{" +
                "PriceArea='" + PriceArea + '\'' +
                ", pricePerKwh=" + pricePerKwh +
                ", timestamp=" + timestamp +
                '}';
    }
}
