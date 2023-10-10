package com.example.user_info_service.pojo;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VehiclePojo {

    private String vehicleNumber;

    private Integer seatCapacity;

    private String imageUrl;

    private Boolean isVehicleAC;

    private Boolean isVehicleSleeper;

}
