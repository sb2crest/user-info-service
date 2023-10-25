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
    public ResponseEntity<String> bookingVehicle(@RequestBody BookingPojo bookingPojo) throws ParseException {
        return new ResponseEntity<>(bookingService.bookingVehicle(bookingPojo), HttpStatus.OK);
    }

    @GetMapping("/bookingDetails")
    public ResponseEntity<BookingDetails> getBookingDetails(@RequestParam("bookingId") String bookingId)  {
        return new ResponseEntity<>(bookingService.getBookingDetails(bookingId), HttpStatus.OK);
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmBooking(@RequestParam("bookingId") String bookingId) {
        return new ResponseEntity<>(bookingService.confirmBooking(bookingId), HttpStatus.OK);
    }

    @GetMapping("/decline")
    public ResponseEntity<String> declineBooking(@RequestParam("bookingId") String bookingId) {
        return new ResponseEntity<>(bookingService.declineBooking(bookingId), HttpStatus.OK);
    }

    @GetMapping("/getBookedSlotsByVehicleNumber")
    public ResponseEntity<VehicleBooked> getBookedSlotsByVehicleNumber(@RequestParam("vehicleNumber") String vehicleNumber) {
        VehicleBooked vehicleBooked = bookingService.getBookedSlotsByVehicleNumber(vehicleNumber);
        return new ResponseEntity<>(vehicleBooked, HttpStatus.OK);
    }

    @PostMapping("/getVehicleAvailability")
    public ResponseEntity<List<VehiclePojo>> getVehicleAvailability(@RequestBody VehiclesAvailable vehiclesAvailable) {
        List<VehiclePojo> vehiclePojo = bookingService.getVehicleAvailability(vehiclesAvailable);
        return new ResponseEntity<>(vehiclePojo, HttpStatus.OK);
    }
    @GetMapping("/getBookingInfoByBookingId")
    public ResponseEntity<BookingInfo> getBookingInfoByBookingId(@RequestParam("bookingId") String bookingId) {
        return new ResponseEntity<>(bookingService.getBookingInfoByBookingId(bookingId), HttpStatus.OK);
    }
}
