package com.user.info.service.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class VehiclesAvailable {

    @NonNull
    private String fromDate;

    @NonNull
    private String toDate;

    private String filter;

    private DistanceRequest distanceRequest;
}
