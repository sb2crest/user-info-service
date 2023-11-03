package com.example.user_info_service.service;

import com.example.user_info_service.dto.*;

import java.text.ParseException;
import java.util.List;

public interface BookingService {
    BookingResponse bookingVehicle(BookingDto bookingDto) throws ParseException;

    BookingData getBookingDetails(String mobile);

    String confirmBooking(String bookingId);

    String declineBooking(String bookingId);

    VehicleBooked getBookedSlotsByVehicleNumber(String vehicleNUmber);

    List<VehicleDto> getVehicleAvailability(VehiclesAvailable vehiclesAvailable);

    BookingInfo getBookingInfoByBookingId(String bookingId);

    void getInTouch(UserData userData) throws Exception;
}
