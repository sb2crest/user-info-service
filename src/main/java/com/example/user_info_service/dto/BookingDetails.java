package com.example.user_info_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingDetails {

    private String bookingId;

    private LocalDate bookingDate;

    private String bookingStatus;

    private Double amountPaid;

    private VehicleDto vehicle;

    private UserDto user;

    private SlotsDto slots;
}
