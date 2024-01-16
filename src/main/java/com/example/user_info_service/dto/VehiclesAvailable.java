package com.example.user_info_service.dto;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
public class VehiclesAvailable {

    @NonNull
    private String fromDate;

    @NonNull
    private String toDate;

    private String filter;

    private DistanceRequest distanceRequest;
}
