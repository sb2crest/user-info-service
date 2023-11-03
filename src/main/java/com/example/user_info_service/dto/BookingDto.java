package com.example.user_info_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Setter
@Getter
public class BookingDto {

    private String vehicleNumber;

    private LocalDate fromDate;

    private LocalDate toDate;

    private UserDto user;

    private SlotsDto slots;
}
