package com.user.info.service.dto;

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

    private Double amount;

    private Double AdvancedPaid;

    private Double TotalAmt;

    private Double RemainingAmt;

    private String bookingId;

    private String bookingStatus;

    private String vehicleAC;

    private String sleeper;

    private Integer seatCapacity;

    private String userName;

    private String mobile;
}
