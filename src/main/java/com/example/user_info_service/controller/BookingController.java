package com.example.user_info_service.controller;

import com.example.user_info_service.exception.NumberException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.pojo.BookingPojo;
import com.example.user_info_service.pojo.UserPojo;
import com.example.user_info_service.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
public class BookingController {
    @Autowired
    BookingService bookingService;

    @PostMapping("/booking")
    void bookingVehicle(@RequestBody BookingPojo bookingPojo) throws ParseException {
        checkMobileNumber(bookingPojo.getUserPojo().getMobile());
        bookingService.bookingVehicle(bookingPojo);
    }
    private void checkMobileNumber(String mobileNumber) {
        if(mobileNumber.isEmpty()){
            throw new NumberException(ResStatus.ENTER_NUMBER);
        }
        if (mobileNumber.length()!=10){
            throw new NumberException(ResStatus.MOBILE_DIGIT);
        }
    }
}
