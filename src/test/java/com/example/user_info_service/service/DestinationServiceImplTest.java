package com.example.user_info_service.service;

import com.example.user_info_service.dto.DistanceRequest;
import com.example.user_info_service.dto.DistanceResponse;
import com.example.user_info_service.entity.DestinationEntity;
import com.example.user_info_service.entity.MasterEntity;
import com.example.user_info_service.repository.DestinationRepository;
import com.example.user_info_service.repository.MasterEntityRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
class DestinationServiceImplTest {

    @InjectMocks
    private DestinationServiceImpl destinationServiceImpl;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private MasterEntityRepo masterEntityRepo;

    @Mock
    private DistanceService distanceService;

    @Test
    public void getAmountDetails_For_Trip() throws IOException {
        when(destinationRepository.getAmountData(Mockito.any())).thenReturn(getDestinationEntity());
        when(masterEntityRepo.findTripAmount(Mockito.any())).thenReturn(getMasterEntity());
        when(distanceService.calculateDistance(Mockito.any())).thenReturn(getDistanceResponse());
        Assertions.assertNotNull(destinationServiceImpl.getAmountDetails(getDistanceRequest()));

    }

    @Test
    public void getAmountDetails_For_Event() throws IOException {
        DistanceRequest distanceRequest = getDistanceRequest();
        distanceRequest.setMultipleDestination(false);
        when(destinationRepository.getAmountData(Mockito.any())).thenReturn(getDestinationEntity());
        when(masterEntityRepo.findTripAmount(Mockito.any())).thenReturn(getMasterEntity());
        when(distanceService.calculateDistance(Mockito.any())).thenReturn(getDistanceResponse());
        Assertions.assertNotNull(destinationServiceImpl.getAmountDetails(distanceRequest));

    }

    @Test
    public void getAmountDetails_For_Event_GreaterThan300KM() throws IOException {
        DistanceRequest distanceRequest = getDistanceRequest();
        distanceRequest.setMultipleDestination(false);
        DistanceResponse distanceResponse = getDistanceResponse();
        distanceResponse.setDistance(350.00);
        when(destinationRepository.getAmountData(Mockito.any())).thenReturn(getDestinationEntity());
        when(masterEntityRepo.findTripAmount(Mockito.any())).thenReturn(getMasterEntity());
        when(distanceService.calculateDistance(Mockito.any())).thenReturn(distanceResponse);
        Assertions.assertNotNull(destinationServiceImpl.getAmountDetails(distanceRequest));

    }

    private DestinationEntity getDestinationEntity() {
        DestinationEntity destinationEntity = new DestinationEntity();
        destinationEntity.setId(1L);
        destinationEntity.setDistance(258.97);
        destinationEntity.setAmount(2568.98);
        return destinationEntity;

    }

    private DistanceResponse getDistanceResponse() {
        DistanceResponse distanceResponse = new DistanceResponse();
        distanceResponse.setDistance(300.00);
        distanceResponse.setMultipleDestination(false);
        return distanceResponse;
    }

    private MasterEntity getMasterEntity() {
        MasterEntity masterEntity = new MasterEntity();
        masterEntity.setId(1L);
        masterEntity.setSource("AND");
        masterEntity.setDestination("ODF");
        masterEntity.setAdvanceAmount(1234.90);
        return masterEntity;
    }

    private DistanceRequest getDistanceRequest() {
        DistanceRequest distanceRequest = new DistanceRequest();
        distanceRequest.setSource("BAC");
        distanceRequest.setDestination("ABC");
        distanceRequest.setSourceLatitude(17.738290);
        distanceRequest.setSourceLongitude(87.738202);
        distanceRequest.setDestinationLatitude(19.098765);
        distanceRequest.setDestinationLongitude(77.998877);
        distanceRequest.setMultipleDestination(true);
        return distanceRequest;
    }

}