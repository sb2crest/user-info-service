package com.example.user_info_service.service;

import com.example.user_info_service.dto.DestinationResponse;
import com.example.user_info_service.dto.DistanceRequest;

import java.io.IOException;
import java.util.List;

public interface DestinationService {
    List<DestinationResponse> getAmountDetails(DistanceRequest distanceRequest) throws IOException;
}
