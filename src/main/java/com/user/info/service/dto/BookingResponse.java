package com.user.info.service.dto;

import lombok.Data;

@Data
public class BookingResponse {

    private String bookingId;

    private String message;

    private int statusCode;
}
