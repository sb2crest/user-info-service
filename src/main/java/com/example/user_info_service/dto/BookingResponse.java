package com.example.user_info_service.dto;

import lombok.Data;

@Data
public class BookingResponse {

    private String bookingId;

    private String message;

    private int statusCode;
}
