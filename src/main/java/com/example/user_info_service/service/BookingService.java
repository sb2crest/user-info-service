package com.example.user_info_service.service;

import com.example.user_info_service.pojo.*;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;

@Service
public interface BookingService {
    String bookingVehicle(BookingPojo bookingPojo) throws ParseException;

    BookingDetails getBookingDetails(String bookingId);

    String confirmBooking(String bookingId);

    String declineBooking(String bookingId);

    VehicleBooked getBookedSlotsByVehicleNumber(String vehicleNUmber);

    List<VehiclePojo> getVehicleAvailability(VehiclesAvailable vehiclesAvailable);
}
