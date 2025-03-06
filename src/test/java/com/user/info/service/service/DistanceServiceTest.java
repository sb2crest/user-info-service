package com.user.info.service.service;

import com.user.info.service.dto.DistanceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistanceServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DistanceService distanceService;

    @Test
    void testCalculateDistance() throws IOException {
        String jsonResponse = "{ \"routes\": [ { \"legs\": [ { \"distance\": { \"text\": \"100 mi\" } } ] } ] }";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);

        assertEquals(100.0, distanceService.calculateDistance(getDistanceRequest()).getDistance());
    }

    @Test
    void testCalculateDistanceIOException() {
        DistanceRequest request = getDistanceRequest();
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(RuntimeException.class);
        assertThrows(RuntimeException.class, () -> distanceService.calculateDistance(request));
    }

    @Test
    void testCalculateDistanceForMultipleDestinations() throws IOException {
        assertTrue(distanceService.calculateDistance(new DistanceRequest()).getMultipleDestination());
    }

    DistanceRequest getDistanceRequest(){
        DistanceRequest distanceRequest = new DistanceRequest();
        distanceRequest.setSourceLatitude(37.7749);
        distanceRequest.setSourceLongitude(-122.4194);
        distanceRequest.setDestinationLatitude(34.0522);
        distanceRequest.setDestinationLongitude(-118.2437);
        distanceRequest.setMultipleDestination(Boolean.FALSE);
        return distanceRequest;
    }
}
