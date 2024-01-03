package com.example.user_info_service.service;

import com.example.user_info_service.dto.DestinationResponse;
import com.example.user_info_service.dto.DistanceRequest;
import com.example.user_info_service.dto.DistanceResponse;
import com.example.user_info_service.entity.DestinationEntity;
import com.example.user_info_service.entity.MasterEntity;
import com.example.user_info_service.model.Constants;
import com.example.user_info_service.repository.DestinationRepository;
import com.example.user_info_service.repository.MasterEntityRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class DestinationServiceImpl implements DestinationService {

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private MasterEntityRepo masterEntityRepo;

    @Autowired
    private DistanceService distanceService;

    @Override
    public DestinationResponse getAmountDetails(DistanceRequest distanceRequest) throws IOException {
        DestinationResponse destinationResponse = new DestinationResponse();
        MasterEntity masterEntity =  getTripOrSourceAndDestination(distanceRequest);

        if (masterEntity != null) {
            setResponse(destinationResponse, masterEntity, distanceRequest);
             return destinationResponse;
        }
        DistanceResponse distanceResponse = distanceService.calculateDistance(distanceRequest);
        log.info("distance {}",distanceResponse.getDistance());
        double distance = roundToNearestMultipleOf10(distanceResponse.getDistance());
        if (distance > Constants.KM) {
            masterEntity = getTripData();
            setResponse(destinationResponse, masterEntity, distanceRequest);
            return destinationResponse;
        }
        DestinationEntity destinationEntity = destinationRepository.getAmountData(distance);
        destinationResponse.setAmount(destinationEntity.getAmount());
        destinationResponse.setSource(distanceRequest.getSource());
        destinationResponse.setDestination(distanceRequest.getDestination());
        return destinationResponse;
    }

    private double roundToNearestMultipleOf10(double value) {
        return Math.round(value / 10.0) * 10.0;
    }

    private void setResponse(DestinationResponse destinationResponse, MasterEntity masterEntity, DistanceRequest distanceRequest) {
        destinationResponse.setSource(distanceRequest.getSource());
        destinationResponse.setDestination(masterEntity.getDestination());
        destinationResponse.setAmount(masterEntity.getAdvanceAmount());
    }

    private MasterEntity getSourceAndDestination(DistanceRequest distanceRequest) {
        return masterEntityRepo.findBySourceAndDestination(distanceRequest.getSource(), distanceRequest.getDestination());
    }

    private MasterEntity getTripData() {
        return masterEntityRepo.findTripAmount(Constants.TRIP);
    }

    private MasterEntity getTripOrSourceAndDestination(DistanceRequest distanceRequest) {
        if (Boolean.TRUE.equals(distanceRequest.getMultipleDestination())) {
            return getTripData();
        } else {
            return getSourceAndDestination(distanceRequest);
        }
    }

}

