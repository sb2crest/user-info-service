package com.example.user_info_service.controller;

import com.example.user_info_service.dto.DestinationResponse;
import com.example.user_info_service.dto.DistanceRequest;
import com.example.user_info_service.service.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class DestinationController {

    @Autowired
    DestinationService destinationService;

    @PostMapping("/getEventType")
    public DestinationResponse getAmountDetails(@RequestBody DistanceRequest distanceRequest) throws IOException {
        return destinationService.getAmountDetails(distanceRequest);
    }
}
