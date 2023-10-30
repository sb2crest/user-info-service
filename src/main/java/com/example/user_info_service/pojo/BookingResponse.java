package com.example.user_info_service.pojo;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class BookingResponse {

    private String bookingId;

    private String message;

    private int statusCode;
}
