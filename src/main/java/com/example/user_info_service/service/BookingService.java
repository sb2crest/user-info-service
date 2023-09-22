package com.example.user_info_service.service;

import com.example.user_info_service.pojo.BookingDetails;
import com.example.user_info_service.pojo.BookingPojo;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public interface BookingService {
    String bookingVehicle(BookingPojo bookingPojo) throws ParseException;

    BookingDetails getBookingDetails(String bookingId);

    String confirmBooking(String bookingId);

    String declineBooking(String bookingId);
}
