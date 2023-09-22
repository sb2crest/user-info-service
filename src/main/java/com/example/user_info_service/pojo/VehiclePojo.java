package com.example.user_info_service.pojo;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VehiclePojo {

    private Integer seatCapacity;

    private String vehicleNumber;

    private String imageUrl;

    private Boolean isVehicleAC;

    private Boolean isVehicleSleeper;

}
