package com.example.user_info_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DestinationResponse {

    private String source;

    private String destination;

    private Double amount;
}
