package com.example.user_info_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Setter
@Getter
public class BookingDto {

    private String vehicleNumber;

    private LocalDate fromDate;

    private LocalDate toDate;

    private LocalDateTime bookedDate;

    private UserDto user;

    private SlotsDto slots;

    private Double totalAmount;
}
