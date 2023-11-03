package com.example.user_info_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class Slots {

    private String vehicleNumber;

    private List<BookedDates> dates;
}
