package com.user.info.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingAccess {

    private List<BookingInfo> upcoming;

    private List<BookingInfo> history;
}
