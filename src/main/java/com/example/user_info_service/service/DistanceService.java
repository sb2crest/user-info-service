package com.example.user_info_service.service;

import com.example.user_info_service.dto.DistanceRequest;
import com.example.user_info_service.dto.DistanceResponse;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class DistanceService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    @Value("${google.maps.url}")
    private String mapUrl;

    private final RestTemplate restTemplate;

    public DistanceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DistanceResponse calculateDistance(DistanceRequest distanceRequest) throws IOException {
        DistanceResponse distanceResponse = new DistanceResponse();
        try {
            if (Boolean.FALSE.equals(distanceRequest.getMultipleDestination())) {
                double distance = getRouteDistance(
                        distanceRequest.getSourceLatitude(), distanceRequest.getSourceLongitude(),
                        distanceRequest.getDestinationLatitude(), distanceRequest.getDestinationLongitude()
                );

                double returningDistance = getRouteDistance(
                        distanceRequest.getDestinationLatitude(), distanceRequest.getDestinationLongitude(),
                        distanceRequest.getSourceLatitude(), distanceRequest.getSourceLongitude()
                );
                double averageDistance = Math.round((distance + returningDistance) / 2);
                distanceResponse.setDistance(averageDistance);
                distanceResponse.setMultipleDestination(Boolean.FALSE);
                return distanceResponse;
            }
        } catch (Exception e){
            throw new BookingException(ResStatus.DISTANCE_CALCULATION);
        }
        distanceResponse.setMultipleDestination(Boolean.TRUE);
        return distanceResponse;
    }

    public Double getRouteDistance(double sourceLat, double sourceLon, double destLat, double destLon) throws IOException {
        try {

            String apiUrl = buildApiUrl(sourceLat, sourceLon, destLat, destLon);
            String jsonResponse = restTemplate.getForObject(apiUrl, String.class);

            return parseDistanceFromJson(jsonResponse);
        } catch (Exception e) {
            throw new BookingException(ResStatus.DISTANCE_CALCULATION_URL);
        }
    }

    private String buildApiUrl(double sourceLat, double sourceLon, double destLat, double destLon) {
        return mapUrl +
                sourceLat + "," + sourceLon +
                "&destination=" + destLat + "," + destLon +
                "&mode=driving" +
                "&key=" + apiKey;
    }

    private Double parseDistanceFromJson(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonObject route = jsonObject.getAsJsonArray("routes").get(0).getAsJsonObject();
        JsonObject legs = route.getAsJsonArray("legs").get(0).getAsJsonObject();
        JsonObject distance = legs.getAsJsonObject("distance");
        String dis = distance.get("text").getAsString();
        return Double.parseDouble(dis.replaceAll("[^\\d.]", ""));
    }
}
