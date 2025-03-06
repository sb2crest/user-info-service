package com.user.info.service.dto;

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

    private Double totalAmount;
}
