package com.example.user_info_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingData {

    private List<BookingDetails> enquiryAndBookedList;

    private List<BookingDetails> historyList;
}
