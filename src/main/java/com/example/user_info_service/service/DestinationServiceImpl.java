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
        MasterEntity masterEntity = getTripOrSourceAndDestination(distanceRequest);

        if (masterEntity != null) {
            setResponse(destinationResponse, masterEntity, distanceRequest);
            return destinationResponse;
        }
        DistanceResponse distanceResponse = distanceService.calculateDistance(distanceRequest);
        log.info("distance {}", distanceResponse.getDistance());
        double distance = roundToNearestMultipleOf10(distanceResponse.getDistance());
        if (distance > Constants.KM) {
            masterEntity = getTripData(distanceRequest);
            setResponse(destinationResponse, masterEntity, distanceRequest);
            return destinationResponse;
        }
        DestinationEntity destinationEntity = destinationRepository.getAmountData(distance, distanceRequest.getVehicleNumber());
        destinationResponse.setTotalAmount(destinationEntity.getAmount() * 2);
        destinationResponse.setAdvanceAmt(destinationResponse.getTotalAmount() * 0.2);
        destinationResponse.setRemainingAmt(destinationResponse.getTotalAmount() - destinationResponse.getAdvanceAmt());
        destinationResponse.setSource(distanceRequest.getSource());
        destinationResponse.setDestination(distanceRequest.getDestination());
        return destinationResponse;
    }

    private double roundToNearestMultipleOf10(double value) {
        return Math.round(value / 10.0) * 10.0;
    }

    private void setResponse(DestinationResponse destinationResponse, MasterEntity masterEntity, DistanceRequest distanceRequest) {
        if (distanceRequest.getMultipleDestination()) {
            destinationResponse.setSource(distanceRequest.getSource());
            destinationResponse.setAdvanceAmt(masterEntity.getAmount());
            destinationResponse.setAmtPerKM(10.00);
        } else {
            destinationResponse.setSource(distanceRequest.getSource());
            destinationResponse.setDestination(masterEntity.getDestination());
            destinationResponse.setTotalAmount(masterEntity.getAmount());
            destinationResponse.setAdvanceAmt(masterEntity.getAmount() * 0.2);
            destinationResponse.setRemainingAmt(masterEntity.getAmount() - masterEntity.getAmount() * 0.2);
        }
    }

    private MasterEntity getSourceAndDestination(DistanceRequest distanceRequest) {
        return masterEntityRepo.findBySourceAndDestination(distanceRequest.getSource(), distanceRequest.getDestination(), distanceRequest.getVehicleNumber());
    }

    private MasterEntity getTripData(DistanceRequest distanceRequest) {
        return masterEntityRepo.findTripAmount(Constants.TRIP, distanceRequest.getVehicleNumber());
    }

    private MasterEntity getTripOrSourceAndDestination(DistanceRequest distanceRequest) {
        if (Boolean.TRUE.equals(distanceRequest.getMultipleDestination())) {
            return getTripData(distanceRequest);
        } else {
            return getSourceAndDestination(distanceRequest);
        }
    }

}

