package DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BatteryInfoResponseDTO {

    @JsonProperty("sim_time_hour")
    private int simTimeHour;

    @JsonProperty("sim_time_min")
    private int simTimeMin;

    @JsonProperty("base_current_load")
    private double baseCurrentLoad;

    @JsonProperty("battery_capacity_kWh")
    private double batteryCapacityKWh;

    @JsonProperty("ev_battery_charge_start_stopp")
    private boolean evBatteryChargeStartStop;

    private static final double EV_BATT_MAX_CAPACITY_KWH = 46.3;

    public BatteryInfoResponseDTO() {}

    public BatteryInfoResponseDTO(int simTimeHour, int simeTime,double basecurrentLoad,
                                  int batteryCapacityKWh, boolean evBatteryChargeStartStop) {
        this.simTimeHour = simTimeHour;
        this.simTimeMin = simeTime;
        this.baseCurrentLoad = basecurrentLoad;
        this.batteryCapacityKWh = batteryCapacityKWh;
        this.evBatteryChargeStartStop = evBatteryChargeStartStop;
    }
    public int getSimTimeHour() {
        return simTimeHour; }
    public void setSimTimeHour(int simTimeHour) {
        this.simTimeHour = simTimeHour; }
    public int getSimTimeMin() {
        return simTimeMin; }
    public void setSimTimeMin(int simTimeMin) {
        this.simTimeMin = simTimeMin; }
    public double getBasecurrentLoad() {
        return baseCurrentLoad; }
    public void setBasecurrentLoad(double basecurrentLoad) {
        this.baseCurrentLoad = basecurrentLoad; }
    public double getBatteryCapacityKWh() {
        return batteryCapacityKWh; }
    public void setBatteryCapacityKWh(int batteryCapacityKWh) {
        this.batteryCapacityKWh = batteryCapacityKWh;
    }
    public boolean isEvBatteryChargeStartStop() {
        return evBatteryChargeStartStop; }
    public void setEvBatteryChargeStartStop(boolean evBatteryChargeStartStop) {
        this.evBatteryChargeStartStop = evBatteryChargeStartStop;
    }

    public double getBatteryPercentage(){
        if(EV_BATT_MAX_CAPACITY_KWH <= 0) return 0;
        return (batteryCapacityKWh/EV_BATT_MAX_CAPACITY_KWH)*100;

    }
    @Override
    public String toString() { //
        return "BatteryInfoResponseDTO{" +
                "simTimeHour=" + simTimeHour +
                ", simTimeMin=" + simTimeMin +
                ", baseCurrentLoad=" + baseCurrentLoad +
                ", batteryCapacityKWh=" + String.format("%.2f", batteryCapacityKWh) + // Format för KWh
                ", evBatteryChargeStartStop=" + evBatteryChargeStartStop +
                ", batteryPercentage=" + String.format("%.2f", getBatteryPercentage()) + // Inkludera procent i toString
                '}';
    }



}
