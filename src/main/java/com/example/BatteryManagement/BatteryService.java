package com.example.BatteryManagement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BatteryService {

    private RestTemplate restTemplate;
    private final String chargingStationUrl;

    private static final double EV_BATT_MAX_CAPACITY_KWH = 46.3;
    private static final double CHARGING_POWER_KW = 7.4;
    private static final double MAX_TOTAL_LOAD_KW = 11.0;
    private static final double BATTERY_CHARGE_THRESHOLD_LOW = 20.0;
    private static final double BATTERY_CHARGE_THRESHOLD_HIGH = 80.0;


    public BatteryService(RestTemplate restTemplate,
                          @Value("${chargingStationUrl}") String chargingStationUrl) {
        this.restTemplate = restTemplate;
        this.chargingStationUrl = chargingStationUrl;
    }

    //För att starta laddningen
    public Battery startCharging() {
        String url = chargingStationUrl + "/charging";
        Battery requestBody = new Battery("on");
        HttpEntity<Battery> request = new HttpEntity<>(requestBody);
        ResponseEntity<Battery> response = restTemplate.exchange
                (url, HttpMethod.POST, request, Battery.class);
        return response.getBody();
    }
    //Ett post anropp till /discharge endpointen för att resetta batteriet
    public Battery resetBatteryToDefault(){
        String url = chargingStationUrl + "/discharge";
        Battery requestBody = new Battery("on");
        HttpEntity<Battery> request = new HttpEntity<>(requestBody);


        ResponseEntity<Battery> response = restTemplate.exchange( // Ändra till ChargeResponseDTO.class
                url,
                HttpMethod.POST,
                request,
                Battery.class);
        return Objects.requireNonNull(response.getBody());
    }


    //Get för att hämta batteriinfo anrop till /info endpointen
    public Battery getBatteryInfo(){
        String url = chargingStationUrl + "/info";
        return restTemplate.getForObject(url, Battery.class);
    }
    //Stoppar laddningen med ett POST anropp till /charging endpointen
    public Battery stopCharging() {
        String url = chargingStationUrl + "/charging";
        Battery requestBody = new Battery("off");
        HttpEntity<Battery> request = new HttpEntity<>(requestBody);

        ResponseEntity<Battery> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Battery.class
        );
        return Objects.requireNonNull(response.getBody());
    }

    //Get anrop till /consumption endpointen
    public List<Double> getHouseHoldConsumptionFor24Hours() {
        String url = chargingStationUrl + "/priceperhour";
        ResponseEntity<List<Double>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,null, new ParameterizedTypeReference<List<Double>>(){}
        );
        return Objects.requireNonNull(response.getBody());
    }
    public double getEvMaxBatteryCapacity(){
        return EV_BATT_MAX_CAPACITY_KWH;
    }


}
