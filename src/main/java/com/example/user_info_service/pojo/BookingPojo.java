package com.example.user_info_service.pojo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Setter
@Getter
public class BookingPojo {

    private String vehicleNumber;

    private LocalDate fromDate;

    private LocalDate toDate;

    private UserPojo user;

    private SlotsPojo slots;
}
