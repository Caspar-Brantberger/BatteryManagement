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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BatteryService {

    private RestTemplate restTemplate;
    private final String chargingStationUrl;

    private static final double CHARGING_POWER_KW = 7.4;
    private static final double MAX_TOTAL_LOAD_KW = 11.5;
    private static final double BATTERY_CHARGE_THRESHOLD_LOW = 20.0;
    private static final double BATTERY_CHARGE_THRESHOLD_HIGH = 80.0;

    private static final int NUMBER_OF_OPTIMAL_HOURS = 4;
    private boolean optimizeByPriceStrategy = false;// False = Lägsta hushållsförbrukning , true = lägsta elpris


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

        System.out.println("determineChargingDecision called with:");
        System.out.println("  batteryPercent=" + currentBatteryPercent);
        System.out.println("  isChargingCurrently=" + isChargingCurrently);
        System.out.println("  currentBaseLoad=" + currentBaseLoad);
        System.out.println("  totalPotentialLoadIfCharging=" + totalPotentialLoadIfCharging);
        System.out.println("  currentSimHour=" + currentSimHour);

        if (currentBatteryPercent >= BATTERY_CHARGE_THRESHOLD_HIGH) {
            System.out.println("Battery level over " + BATTERY_CHARGE_THRESHOLD_HIGH + "%. Stops charging.");
            return false; // Stoppa laddningen
        }

        if (totalPotentialLoadIfCharging > MAX_TOTAL_LOAD_KW) {
            System.out.println("Total load over " + MAX_TOTAL_LOAD_KW + "kW. Stops charging.");
            return false; // Stoppa laddningen
        }


        List<Integer> optimalHours = findOptimalChargingHours(
                allHourlyPrices, allHourlyConsumption, optimizeByPriceStrategy, NUMBER_OF_OPTIMAL_HOURS);


        if (optimalHours.contains(currentSimHour)) {

            if (!isChargingCurrently) {
                System.out.println("Optimal hour and conditions met. STARTING CHARGING!");
            }
            return true;
        } else {

            if (isChargingCurrently) {
                System.out.println("Not optimal hour. STOPPING CHARGING if currently on.");
            } else {
                System.out.println("Not optimal hour. Not starting charging.");
            }
            return false;
        }
    }


    private void executeChargingCommand(boolean shouldCharge, boolean isChargingCurrently) {
        System.out.println("executeChargingCommand called with shouldCharge=" + shouldCharge + ", isChargingCurrently=" + isChargingCurrently);

        if (shouldCharge && !isChargingCurrently) {
            ChargeResponseDTO response = startCharging();
            System.out.println("COMMAND EXECUTE: STARTING CHARGING! Status: " + response.getCharging());

        } else if (!shouldCharge && isChargingCurrently) {
            ChargeResponseDTO response = stopCharging();
            System.out.println("COMMAND EXECUTE: STOPPING CHARGING! Status: " + response.getCharging());
        } else {
            System.out.println("No change in charging state needed");
        }
    }


    private List<Integer> findOptimalChargingHours(
            List<Double> allHourlyPrices,
            List<Double> allHourlyConsumption,
            boolean optimizeByPrice,
            int numberOfBestHours
    ) {

        List<Map.Entry<Integer, Double>> hourlyScores = new ArrayList<>();

        for (int i = 0; i < 24; i++) {
            double currentHourConsumption = allHourlyConsumption.get(i);
            double currentHourPrice = allHourlyPrices.get(i);
            double totalLoadWithCharging = currentHourConsumption + CHARGING_POWER_KW;

            if (totalLoadWithCharging <= MAX_TOTAL_LOAD_KW) {
                double score = optimizeByPrice ? currentHourPrice : currentHourConsumption; // Välj score baserat på optimeringsstrategi
                hourlyScores.add(new AbstractMap.SimpleEntry<>(i, score));
            }
        }


        hourlyScores.sort(Comparator.comparingDouble(Map.Entry::getValue));

        return hourlyScores.stream()
                .limit(numberOfBestHours)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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

