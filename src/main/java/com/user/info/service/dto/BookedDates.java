package com.user.info.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;


@Data
@AllArgsConstructor
public class BookedDates {

    private String date;

    private Boolean isBooked;
}
