package com.example.user_info_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;


@Data
@AllArgsConstructor
public class BookedDates {

    private LocalDate date;

    private Boolean isBooked;
}
