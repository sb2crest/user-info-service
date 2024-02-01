package com.example.user_info_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingData {

    private List<BookingDetails> bookedList;

    private List<BookingDetails> historyList;

    private List<BookingDetails> enquiryList;
}
