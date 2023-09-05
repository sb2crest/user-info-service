package com.example.user_info_service.service;

import com.example.user_info_service.pojo.BookingPojo;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public interface BookingService {
    void bookingVehicle(BookingPojo bookingPojo) throws ParseException;
}
