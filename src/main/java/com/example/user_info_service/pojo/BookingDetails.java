package com.example.user_info_service.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingDetails {

    private VehiclePojo vehicle;

    private UserPojo user;

    private SlotsPojo slots;
}
