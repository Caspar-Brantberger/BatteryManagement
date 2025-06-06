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

import java.util.List;
import java.util.Objects;

@Service
public class BatteryService {

    private RestTemplate restTemplate;
    private final String chargingStationUrl;

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
    public ChargeResponseDTO startCharging() {
        String url = chargingStationUrl + "/charge";
        ChargeRequestDTO requestBody = new ChargeRequestDTO("on");
        HttpEntity<ChargeRequestDTO> requestEntity = new HttpEntity<>(requestBody);
        ResponseEntity<ChargeResponseDTO> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, ChargeResponseDTO.class);
        return Objects.requireNonNull(response.getBody());

    }
    //Ett post anropp till /discharge endpointen för att resetta batteriet
    public ChargeResponseDTO resetBatteryToDefault(){
        String url = chargingStationUrl + "/discharge";
        ChargeRequestDTO requestBody = new ChargeRequestDTO("on");
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
        return restTemplate.getForObject(url, BatteryInfoResponseDTO.class);
    }
    //Stoppar laddningen med ett POST anropp till /charging endpointen
    public ChargeResponseDTO stopCharging() {
        String url = chargingStationUrl + "/charging";
        ChargeRequestDTO requestBody = new ChargeRequestDTO("off");
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


}
