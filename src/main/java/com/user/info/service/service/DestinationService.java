package com.user.info.service.service;

import com.user.info.service.dto.DestinationResponse;
import com.user.info.service.dto.DistanceRequest;

import java.io.IOException;
import java.util.List;

public interface DestinationService {
    List<DestinationResponse> getAmountDetails(DistanceRequest distanceRequest) throws IOException;
}
