package com.example.user_info_service.service;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.SlotsEntity;
import com.example.user_info_service.entity.UserEntity;
import com.example.user_info_service.pojo.BookingPojo;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.SlotsRepo;
import com.example.user_info_service.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Component
public class BookingServiceImpl implements BookingService {

    @Autowired
    UserRepo userRepo;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    @Autowired
    SlotsRepo slotsRepo;
    @Autowired
    BookingRepo bookingRepo;
    @Override
    public void bookingVehicle(BookingPojo bookingPojo) throws ParseException {
        UserEntity userEntity = new UserEntity();
        userEntity.setName(bookingPojo.getUserPojo().getName());
        userEntity.setEmail(bookingPojo.getUserPojo().getEmail());
        userEntity.setMobile(bookingPojo.getUserPojo().getMobile());
        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setVehicleNumber(bookingPojo.getVehicleNumber());
        Date startDate = dateFormat.parse(bookingPojo.getFromDate());
        Date endDate = dateFormat.parse(bookingPojo.getToDate());
        bookingEntity.setFromDate(String.valueOf(startDate));
        bookingEntity.setToDate(String.valueOf(endDate));
        bookingEntity.setUserEntity(userEntity);
        SlotsEntity slotsEntity = new SlotsEntity();
        slotsEntity.setBookingEntity( bookingEntity);
        Boolean vehicleAvailability = slotsRepo.findVehicleAvailabilityOnRequiredDate(slotsEntity.getFromDate(),slotsEntity.getToDate());
        if (Boolean.TRUE.equals(vehicleAvailability)){
            slotsEntity.setIsAvailable(true);
            userRepo.save(userEntity);
            bookingRepo.save(bookingEntity);
            slotsRepo.save(slotsEntity);
        }
    }

}
