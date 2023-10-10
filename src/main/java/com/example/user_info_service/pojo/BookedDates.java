package com.example.user_info_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class BookedDates {

    private String date;

    private Boolean isBooked;
}
