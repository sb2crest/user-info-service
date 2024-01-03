package com.example.user_info_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistanceRequest {

    private String source;

    private String destination;

    private Double sourceLatitude;

    private Double sourceLongitude;

    private Double destinationLatitude;

    private Double destinationLongitude;

    private Boolean multipleDestination;
}
