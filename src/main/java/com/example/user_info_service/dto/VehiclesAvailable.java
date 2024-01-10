package com.example.user_info_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VehiclesAvailable {

    private String fromDate;

    private String toDate;

    private String filter;

    private DistanceRequest distanceRequest;
}
