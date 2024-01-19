package com.example.user_info_service.util;

import com.example.user_info_service.entity.*;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.model.GmailValidator;

import java.util.List;

public class Validation {



    public static void userMobileValidation(String mobile) {
        if (mobile.isEmpty()) {
            throw new BookingException(ResStatus.ENTER_NUMBER);
        }
        if (mobile.length() != 10) {
            throw new BookingException(ResStatus.MOBILE_DIGIT);
        }
    }

    public static void userEmailValidation(String email) {
        if (email == null || !GmailValidator.isValidGmail(email)) {
            throw new BookingException(ResStatus.INVALID_EMAIL);
        }
    }

    public static void validateBookingEntity(BookingEntity bookingEntity) {
        if (bookingEntity == null) {
            throw new BookingException(ResStatus.BOOKING_NOT_FOUND);
        }
    }

    public static void validateBookingEntityList(List<BookingEntity> bookingEntityList) {
        if (bookingEntityList == null || bookingEntityList.isEmpty()) {
            throw new BookingException(ResStatus.BOOKING_DATA_NOT_FOUND_WITH_MOBILE);
        }
    }

    public static void validateVehicleEntity(VehicleEntity vehicleEntity) {
        if (vehicleEntity == null) {
            throw new BookingException(ResStatus.VEHICLE_NOT_FOUND);
        }
    }

    public static void validateUserEntity(UserEntity userEntity) {
        if (userEntity == null) {
            throw new BookingException(ResStatus.USER_NOT_FOUND);
        }
    }

    public static void validateSlotEntity(SlotsEntity slotsEntity) {
        if (slotsEntity == null) {
            throw new BookingException(ResStatus.SLOTS_NOT_FOUND);
        }
    }

    public static void validatePaymentEntityList(List<PaymentEntity> paymentEntity) {
        if (paymentEntity == null || paymentEntity.isEmpty()) {
            throw new BookingException(ResStatus.PAYMENT_DETAILS_NOT_FOUND);
        }
    }
}
