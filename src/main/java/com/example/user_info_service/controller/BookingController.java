package com.example.user_info_service.controller;

import com.example.user_info_service.pojo.*;
import com.example.user_info_service.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@Slf4j
public class BookingController {
    @Autowired
    BookingService bookingService;

    @PostMapping("/booking")
    ResponseEntity<String> bookingVehicle(@RequestBody BookingPojo bookingPojo) throws ParseException {
        return new ResponseEntity<>(bookingService.bookingVehicle(bookingPojo), HttpStatus.OK);
    }

    @GetMapping("/bookingDetails")
    ResponseEntity<BookingDetails> getBookingDetails(@RequestParam("bookingId") String bookingId)  {
        return new ResponseEntity<>(bookingService.getBookingDetails(bookingId), HttpStatus.OK);
    }

    @GetMapping("/confirm")
    ResponseEntity<String> confirmBooking(@RequestParam("bookingId") String bookingId) {
        return new ResponseEntity<>(bookingService.confirmBooking(bookingId), HttpStatus.OK);
    }

    @GetMapping("/decline")
    ResponseEntity<String> declineBooking(@RequestParam("bookingId") String bookingId) {

        return new ResponseEntity<>(bookingService.declineBooking(bookingId), HttpStatus.OK);
    }

    @GetMapping("/getBookedSlotsByVehicleNumber")
    ResponseEntity<VehicleBooked> getBookedSlotsByVehicleNumber(@RequestParam("vehicleNumber") String vehicleNUmber) {
        VehicleBooked vehicleBooked = bookingService.getBookedSlotsByVehicleNumber(vehicleNUmber);
        return new ResponseEntity<>(vehicleBooked, HttpStatus.OK);
    }

    @PostMapping("/getVehicleAvailability")
    ResponseEntity<List<VehiclePojo>> getVehicleAvailability(@RequestBody VehiclesAvailable vehiclesAvailable) {
        List<VehiclePojo> vehiclePojo = bookingService.getVehicleAvailability(vehiclesAvailable);
        return new ResponseEntity<>(vehiclePojo, HttpStatus.OK);
    }


}
