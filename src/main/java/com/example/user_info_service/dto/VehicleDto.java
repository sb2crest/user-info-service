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

    private String vehicleAC;

    private String sleeper;

    private MultipartFile image;

    private String driverName;

    private String driverNumber;

    private String alternateNumber;

    private String emergencyNumber;

    private Double totalAmount;

    private Double advanceAmt;

    private Double remainingAmt;

    private Double amtPerKM;

    private String source;

    private String destination;

}
