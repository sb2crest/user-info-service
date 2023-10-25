package com.example.user_info_service.service;

import com.example.user_info_service.pojo.*;

import java.text.ParseException;
import java.util.List;

public interface BookingService {
    String bookingVehicle(BookingPojo bookingPojo) throws ParseException;

    BookingDetails getBookingDetails(String bookingId);

    String confirmBooking(String bookingId);

    String declineBooking(String bookingId);

    VehicleBooked getBookedSlotsByVehicleNumber(String vehicleNUmber);

    List<VehiclePojo> getVehicleAvailability(VehiclesAvailable vehiclesAvailable);

    BookingInfo getBookingInfoByBookingId(String bookingId);
}
