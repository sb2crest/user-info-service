package com.example.user_info_service.dto;


import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Setter
@Getter
public class VehicleDto {

    private String vehicleNumber;

    private Integer seatCapacity;

    private List<String> s3ImageUrl;

    private Boolean isVehicleAC;

    private Boolean isVehicleSleeper;

    private MultipartFile image;

    private String driverName;

    private String driverNumber;

    private String alternateNumber;

    private String emergencyNumber;

}
