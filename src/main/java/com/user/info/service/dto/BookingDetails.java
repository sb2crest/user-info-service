package com.user.info.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingDetails {

    private String bookingId;

    private LocalDate bookingDate;

    private String bookingStatus;

    private Double AdvancedPaid;

    private Double TotalAmt;

    private Double RemainingAmt;

    private VehicleDto vehicle;

    private UserDto user;

    private SlotsDto slots;
}
