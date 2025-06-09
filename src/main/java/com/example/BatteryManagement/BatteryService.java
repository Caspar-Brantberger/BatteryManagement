package com.example.BatteryManagement;

import DTO.BatteryInfoResponseDTO;
import DTO.ChargeRequestDTO;
import DTO.ChargeResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLOutput;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BatteryService {

    private RestTemplate restTemplate;
    private final String chargingStationUrl;

    private static final double CHARGING_POWER_KW = 7.4;
    private static final double MAX_TOTAL_LOAD_KW = 11.0;
    private static final double BATTERY_CHARGE_THRESHOLD_LOW = 20.0;
    private static final double BATTERY_CHARGE_THRESHOLD_HIGH = 80.0;


    public BatteryService(RestTemplate restTemplate,
                          @Value("${charging.station.url}") String chargingStationUrl) {
        this.restTemplate = restTemplate;
        this.chargingStationUrl = chargingStationUrl;
    }

    //För att starta laddningen
    public ChargeResponseDTO startCharging() {
        String url = chargingStationUrl + "/charge";
        ChargeRequestDTO requestBody = new ChargeRequestDTO();
        requestBody.setCharging("on");

        HttpEntity<ChargeRequestDTO> requestEntity = new HttpEntity<>(requestBody);
        ResponseEntity<ChargeResponseDTO> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, ChargeResponseDTO.class);
        return Objects.requireNonNull(response.getBody());

    }
    //Ett post anropp till /discharge endpointen för att resetta batteriet
    public ChargeResponseDTO resetBatteryToDefault(){
        String url = chargingStationUrl + "/discharge";
        ChargeRequestDTO requestBody = new ChargeRequestDTO();
        requestBody.setDischarging("on");

        HttpEntity<ChargeRequestDTO> requestEntity = new HttpEntity<>(requestBody);
        ResponseEntity<ChargeResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                ChargeResponseDTO.class
        );
        return Objects.requireNonNull(response.getBody());

    }


    //Get för att hämta batteriinfo anrop till /info endpointen
    public BatteryInfoResponseDTO getBatteryInfo(){
        String url = chargingStationUrl + "/info";
        System.out.println("DEBUG: Anropar URL: " + url);
        return restTemplate.getForObject(url, BatteryInfoResponseDTO.class);
    }
    //Stoppar laddningen med ett POST anropp till /charging endpointen
    public ChargeResponseDTO stopCharging() {
        String url = chargingStationUrl + "/charge";
        ChargeRequestDTO requestBody = new ChargeRequestDTO();
        requestBody.setCharging("off");

        HttpEntity<ChargeRequestDTO> requestEntity = new HttpEntity<>(requestBody);
        ResponseEntity<ChargeResponseDTO> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, ChargeResponseDTO.class);
        return Objects.requireNonNull(response.getBody());

    }

    //Get anrop till /consumption endpointen
    public List<Double> getHouseHoldConsumptionFor24Hours() {
        String url = chargingStationUrl + "/baseload";
        ResponseEntity<List<Double>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,null, new ParameterizedTypeReference<List<Double>>(){}
        );
        return Objects.requireNonNull(response.getBody());
    }
    public List<Double> getEnergyPricesFor24Hours() {
        String url = chargingStationUrl + "/priceperhour";
        ResponseEntity<List<Double>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Double>>() {});
        return Objects.requireNonNull(response.getBody());
    }


    public void runOptimizedChargingCycle() {
        try {

            List<Double> allHourlyPrices = getEnergyPricesFor24Hours();
            List<Double> allHourlyConsumption = getHouseHoldConsumptionFor24Hours();
            logInitialInfo(allHourlyPrices, allHourlyConsumption);

            while (true) {
                BatteryInfoResponseDTO currentInfo = getBatteryInfo();

                boolean shouldCharge = determineChargingDecision(currentInfo, allHourlyPrices, allHourlyConsumption);

                executeChargingCommand(shouldCharge, currentInfo.isEvBatteryChargeStartStop());

                logCurrentStatus(currentInfo, shouldCharge);
                Thread.sleep(4000);
            }
        } catch (InterruptedException e) {
            System.err.println("Optimization cycle interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private boolean determineChargingDecision(
            BatteryInfoResponseDTO currentInfo,
            List<Double> allHourlyPrices,
            List<Double> allHourlyConsumption) {

        double currentBatteryPercent = currentInfo.getBatteryPercentage();
        boolean isChargingCurrently = currentInfo.isEvBatteryChargeStartStop();
        double currentBaseLoad = currentInfo.getBasecurrentLoad();
        int currentSimHour = currentInfo.getSimTimeHour();
        double totalPotentialLoadIfCharging = currentBaseLoad + CHARGING_POWER_KW;


        if (currentBatteryPercent >= BATTERY_CHARGE_THRESHOLD_HIGH) {
            System.out.println("Battery level over " + BATTERY_CHARGE_THRESHOLD_HIGH + "%. Stops charging.");
            return false;
        }
        if (totalPotentialLoadIfCharging > MAX_TOTAL_LOAD_KW) {
            if (isChargingCurrently) {
                System.out.println("Total load over " + MAX_TOTAL_LOAD_KW + "kW. Stops charging.");
            }
            return false;
        }


        if (currentBatteryPercent < BATTERY_CHARGE_THRESHOLD_LOW) {
            System.out.println("Battery level below " + BATTERY_CHARGE_THRESHOLD_LOW + "%. Considering charging...");

            Optional<Integer> optimalHour = findOptimalChargingHour(allHourlyPrices, allHourlyConsumption);


            if (optimalHour.isPresent() && optimalHour.get() == currentSimHour) {
                return true;
            } else {
                System.out.println("Current hour (" + String.format("%02d:00", currentSimHour) + ") is not optimal. Optimal is " + (optimalHour.isPresent() ? String.format("%02d:00", optimalHour.get()) : "none found") + ". Waiting.");
                return false;
            }
        }


        return isChargingCurrently;
    }


    private void executeChargingCommand(boolean shouldCharge, boolean isChargingCurrently) {
        if (shouldCharge && !isChargingCurrently) {
            ChargeResponseDTO response = startCharging();
            System.out.println("COMMAND EXECUTE: STARTING CHARGING! Status: " + response.getCharging());
        } else if (!shouldCharge && isChargingCurrently) {
            ChargeResponseDTO response = stopCharging();
            System.out.println("COMMAND EXECUTE: STOPPING CHARGING! Status: " + response.getCharging());
        }
    }


    private Optional<Integer> findOptimalChargingHour(List<Double> allHourlyPrices, List<Double> allHourlyConsumption) {
        int bestHour = -1;
        double minScore = Double.MAX_VALUE;

        for (int i = 0; i < 24; i++) {
            double currentHourConsumption = allHourlyConsumption.get(i);
            double currentHourPrice = allHourlyPrices.get(i);
            double totalLoadWithCharging = currentHourConsumption + CHARGING_POWER_KW;

            if (totalLoadWithCharging <= MAX_TOTAL_LOAD_KW) {
                double currentScore = currentHourPrice;

                if (currentScore < minScore) {
                    minScore = currentScore;
                    bestHour = i;
                }
            }
        }
        return bestHour != -1 ? Optional.of(bestHour) : Optional.empty();
    }


    private void logInitialInfo(List<Double> prices, List<Double> consumption) {
        System.out.println("--------------------------------------------------");
        System.out.println("Optimized charging cycle started");
        System.out.println("Electricity prices (Öre/kWh): " + prices);
        System.out.println("Base consumption (kW): " + consumption);
        System.out.println("--------------------------------------------------");
    }

    private void logCurrentStatus(BatteryInfoResponseDTO currentInfo, boolean currentDecisionToCharge) {
        double currentBatteryPercent = currentInfo.getBatteryPercentage();
        double currentBaseLoad = currentInfo.getBasecurrentLoad();
        double totalPotentialLoad = currentBaseLoad + CHARGING_POWER_KW;

        System.out.println("\n--- Current Simulated Time: " + String.format("%02d:%02d", currentInfo.getSimTimeHour(), currentInfo.getSimTimeMin()) + " ---");
        System.out.println("Battery level: " + String.format("%.2f", currentBatteryPercent) + "%");
        System.out.println("Household base load: " + String.format("%.2f", currentBaseLoad) + " kW");
        System.out.println("Potential total load (incl. charging): " + String.format("%.2f", totalPotentialLoad) + " kW (Max " + MAX_TOTAL_LOAD_KW + " kW)");
        System.out.println("Decision: " + (currentDecisionToCharge ? "CHARGING ON" : "CHARGING OFF"));
        System.out.println("--------------------------------------------------");
    }
}

