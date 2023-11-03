package com.example.user_info_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VehiclesAvailable {

    private LocalDate fromDate;

    private LocalDate toDate;

    private Boolean isAC;

    private Boolean isSleeper;

}
