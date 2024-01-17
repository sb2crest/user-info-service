package com.example.user_info_service.service;

import com.example.user_info_service.dto.DestinationResponse;
import com.example.user_info_service.dto.DistanceRequest;
import com.example.user_info_service.dto.DistanceResponse;
import com.example.user_info_service.entity.DestinationEntity;
import com.example.user_info_service.entity.MasterEntity;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.model.Constants;
import com.example.user_info_service.repository.DestinationRepository;
import com.example.user_info_service.repository.MasterEntityRepo;
import com.example.user_info_service.repository.VehicleInfoRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DestinationServiceImpl implements DestinationService {

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private MasterEntityRepo masterEntityRepo;

    @Autowired
    private DistanceService distanceService;

    @Autowired
    private VehicleInfoRepo vehicleInfoRepo;

    @Override
    public List<DestinationResponse>  getAmountDetails(DistanceRequest distanceRequest) throws IOException {
        List<DestinationResponse> destinationResponses = new ArrayList<>();
        List<MasterEntity> masterEntities = getTripOrSourceAndDestination(distanceRequest);
        Double distance = 0.00;
        if(!distanceRequest.getMultipleDestination()) {
            DistanceResponse distanceResponse = distanceService.calculateDistance(distanceRequest);
            log.info("distance {}", distanceResponse.getDistance());
            distance = roundToNearestMultipleOf10(distanceResponse.getDistance());
        }
        if (!masterEntities.isEmpty()) {
            setResponse(destinationResponses, masterEntities, distanceRequest);
        }
        if (masterEntities.isEmpty() && distance > Constants.KM) {
            masterEntities = getTripAmount(distanceRequest);
            setResponse(destinationResponses, masterEntities, distanceRequest);
        }
        if (destinationResponses.isEmpty()) {
            List<DestinationEntity> destinationEntities = destinationRepository.getAmountData(distance, distanceRequest.getVehicleNumbers());
            getEventDetails(destinationEntities, destinationResponses, distanceRequest);
        }

        return destinationResponses;
    }

    private void getEventDetails(List<DestinationEntity> destinationEntities, List<DestinationResponse> destinationResponses, DistanceRequest distanceRequest) {
        for (DestinationEntity destinationEntity : destinationEntities) {
            DestinationResponse destinationResponse = new DestinationResponse();
            destinationResponse.setTotalAmount(destinationEntity.getAmount());
            destinationResponse.setAdvanceAmt(destinationResponse.getTotalAmount() * 0.2);
            destinationResponse.setRemainingAmt(destinationResponse.getTotalAmount() - destinationResponse.getAdvanceAmt());
            destinationResponse.setSource(distanceRequest.getSource());
            destinationResponse.setDestination(distanceRequest.getDestination());
            destinationResponse.setVehicleNumber(destinationEntity.getVehicleNumber());

            destinationResponses.add(destinationResponse);
        }
    }

    private double roundToNearestMultipleOf10(double value) {
        return Math.round(value / 10.0) * 10.0;
    }

    private void setResponse(List<DestinationResponse> destinationResponses, List<MasterEntity> masterEntities, DistanceRequest distanceRequest) {
        for (MasterEntity masterEntity : masterEntities) {
            DestinationResponse destinationResponse = new DestinationResponse();
            if (distanceRequest.getMultipleDestination()) {
                destinationResponse.setSource(distanceRequest.getSource());
                destinationResponse.setAdvanceAmt(masterEntity.getAmount());
                VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(masterEntity.getVehicleNumber());
                destinationResponse.setAmtPerKM(vehicleEntity.getAmtPerKM());
                destinationResponse.setVehicleNumber(masterEntity.getVehicleNumber());
            } else {
                destinationResponse.setTotalAmount(masterEntity.getAmount());
                destinationResponse.setAdvanceAmt(destinationResponse.getTotalAmount() * 0.2);
                destinationResponse.setRemainingAmt(destinationResponse.getTotalAmount() - destinationResponse.getAdvanceAmt());
                destinationResponse.setSource(distanceRequest.getSource());
                destinationResponse.setDestination(distanceRequest.getDestination());
                destinationResponse.setVehicleNumber(masterEntity.getVehicleNumber());
            }
            destinationResponses.add(destinationResponse);
        }
    }

    private List<MasterEntity> getSourceAndDestination(DistanceRequest distanceRequest) {
        List<String> vehicleNumbers = distanceRequest.getVehicleNumbers();
        return masterEntityRepo.findBySourceAndDestination(
                distanceRequest.getSource(),
                distanceRequest.getDestination(),
                vehicleNumbers
        );
    }

    private List<MasterEntity> getTripAmount(DistanceRequest distanceRequest) {
        List<String> vehicleNumbers = distanceRequest.getVehicleNumbers();
        return masterEntityRepo.findTripAmount(Constants.TRIP, vehicleNumbers);
    }

    private List<MasterEntity> getTripOrSourceAndDestination(DistanceRequest distanceRequest) {
        if (Boolean.TRUE.equals(distanceRequest.getMultipleDestination())) {
            return getTripAmount(distanceRequest);
        } else {
            return getSourceAndDestination(distanceRequest);
        }
    }

}

