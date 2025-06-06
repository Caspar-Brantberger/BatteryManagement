package com.example.BatteryManagement;

import DTO.BatteryInfoResponseDTO;
import DTO.ChargeResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/battery")
public class BatteryController {

    private final BatteryService batteryService;

    public BatteryController(BatteryService batteryService) {
        this.batteryService = batteryService;
    }

    @GetMapping("/start-optimaztion")
    public String startOpimization() {
        new Thread(()-> batteryService.runOptimizedChargingCycle()).start();
        return "Battery optimization started.";
    }
    @GetMapping("/status")
    public BatteryInfoResponseDTO getCurrentBatteryStatus() {
        return batteryService.getBatteryInfo();
    }
    @PostMapping("/charge/start")
    public ChargeResponseDTO startCharging(){
        return batteryService.startCharging();
    }
    @PostMapping("/charge/stop")
    public ChargeResponseDTO stopCharging(){
        return batteryService.stopCharging();
    }
    @PostMapping("/reset")
    public ChargeResponseDTO resetBatteryToDefault() {
        return batteryService.resetBatteryToDefault();
    }
}
