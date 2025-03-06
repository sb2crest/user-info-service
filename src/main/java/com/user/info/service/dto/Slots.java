package com.user.info.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class Slots {

    private String vehicleNumber;

    private List<BookedDates> dates;
}
