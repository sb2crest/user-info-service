package com.example.user_info_service.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VehicleDto {

    private String vehicleNumber;

    private Integer seatCapacity;

    private String imageUrl;

    private Boolean isVehicleAC;

    private Boolean isVehicleSleeper;

}
