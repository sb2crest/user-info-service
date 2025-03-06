package com.user.info.service.dto;

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
