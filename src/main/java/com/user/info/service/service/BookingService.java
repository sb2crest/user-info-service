package com.user.info.service.service;

import com.example.user_info_service.dto.*;
import com.user.info.service.dto.*;

import java.text.ParseException;
import java.util.List;

public interface BookingService {
    BookingResponse bookingVehicle(BookingDto bookingDto) throws ParseException;

    BookingData getBookingDetails(String mobile);

    String confirmBooking(String bookingId);

    String declineBooking(String bookingId);

    VehicleBooked getBookedSlotsByVehicleNumber(String vehicleNUmber);

    List<VehicleDto> getVehicleAvailability(VehiclesAvailable vehiclesAvailable);

    BookingAccess getBookingInfoByMobile(String mobile);

    void getInTouch(UserData userData) throws Exception;
}
