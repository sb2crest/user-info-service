package com.example.user_info_service.service;

import com.example.user_info_service.dto.DestinationResponse;
import com.example.user_info_service.dto.DistanceRequest;

import java.io.IOException;

public interface DestinationService {
    DestinationResponse getAmountDetails(DistanceRequest distanceRequest) throws IOException;
}
