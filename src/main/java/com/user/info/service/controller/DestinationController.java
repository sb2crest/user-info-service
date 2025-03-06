package com.user.info.service.controller;

import com.user.info.service.dto.DestinationResponse;
import com.user.info.service.dto.DistanceRequest;
import com.user.info.service.service.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class DestinationController {

    @Autowired
    DestinationService destinationService;

    @PostMapping("/getEventType")
    public List<DestinationResponse> getAmountDetails(@RequestBody DistanceRequest distanceRequest) throws IOException {
        return destinationService.getAmountDetails(distanceRequest);
    }
}
