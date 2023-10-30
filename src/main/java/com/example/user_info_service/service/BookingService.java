package com.example.user_info_service.service;

import com.example.user_info_service.pojo.*;

import javax.mail.MessagingException;
import java.text.ParseException;
import java.util.List;

public interface BookingService {
    BookingResponse bookingVehicle(BookingPojo bookingPojo) throws ParseException;

    BookingDetails getBookingDetails(String bookingId);

    String confirmBooking(String bookingId);

    String declineBooking(String bookingId);

    VehicleBooked getBookedSlotsByVehicleNumber(String vehicleNUmber);

    List<VehiclePojo> getVehicleAvailability(VehiclesAvailable vehiclesAvailable);

    BookingInfo getBookingInfoByBookingId(String bookingId);

    void getInTouch(UserData userData) throws Exception;
}
