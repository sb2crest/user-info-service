package com.example.user_info_service.dto;

import lombok.Data;

@Data
public class BookingInfo {

    private String driverName;

    private String driverNumber;

    private String alternateNumber;

    private String vehicleNumber;

    private String fromDate;

    private String toDate;

    private String bookingDate;
}
