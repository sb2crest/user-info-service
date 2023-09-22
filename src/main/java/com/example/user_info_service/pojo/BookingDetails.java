package com.example.user_info_service.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingDetails {

    private VehiclePojo vehiclePojo;

    private UserPojo userPojo;

    private SlotsPojo slotsPojo;
}
