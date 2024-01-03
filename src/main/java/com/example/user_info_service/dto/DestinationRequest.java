package com.example.user_info_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DestinationRequest {

    private Double distance;

    private Boolean multipleDestination;
}
