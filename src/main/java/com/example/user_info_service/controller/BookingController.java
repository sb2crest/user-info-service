package com.example.user_info_service.controller;

import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.model.GmailValidator;
import com.example.user_info_service.pojo.*;
import com.example.user_info_service.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@Slf4j
public class BookingController {
    @Autowired
    BookingService bookingService;

    @PostMapping("/booking")
    ResponseEntity<String> bookingVehicle(@RequestBody BookingPojo bookingPojo) throws ParseException {
        try {
            checkMobileNumber(bookingPojo.getUserPojo().getMobile());
            emailValidation(bookingPojo.getUserPojo().getEmail());
            return new ResponseEntity<>(bookingService.bookingVehicle(bookingPojo), HttpStatus.OK);
        } catch (Exception e) {
            log.info("exception :" + e.getMessage());
            throw e;
        }
    }

    private void emailValidation(String email) {
        if (!email.isBlank() && !GmailValidator.isValidGmail(email)) {
            throw new BookingException(ResStatus.INVALID_EMAIL);
        }
    }

    private void checkMobileNumber(String mobileNumber) {
        if (mobileNumber.isEmpty()) {
            throw new BookingException(ResStatus.ENTER_NUMBER);
        }
        if (mobileNumber.length() != 10) {
            throw new BookingException(ResStatus.MOBILE_DIGIT);
        }
    }

    @GetMapping("/bookingDetails")
    ResponseEntity<BookingDetails> getBookingDetails(@RequestParam("bookingId") String bookingId) {
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
        VehicleBooked  vehicleBooked = bookingService.getBookedSlotsByVehicleNumber(vehicleNUmber);
        return new ResponseEntity<>(vehicleBooked, HttpStatus.OK);
    }



}
