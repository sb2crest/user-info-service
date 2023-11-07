package com.example.user_info_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingAccess {

    private List<BookingInfo> upcoming;

    private List<BookingInfo> history;
}
