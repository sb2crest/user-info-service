package com.example.user_info_service.service;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.SlotsEntity;
import com.example.user_info_service.entity.UserEntity;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.pojo.*;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.SlotsRepo;
import com.example.user_info_service.repository.UserRepo;
import com.example.user_info_service.repository.VehicleInfoRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.text.*;
import java.util.*;


@Component
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    UserRepo userRepo;
    @Autowired
    SlotsRepo slotsRepo;
    @Autowired
    BookingRepo bookingRepo;
    @Autowired
    VehicleInfoRepo vehicleInfoRepo;

    private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    @Transactional
    public String bookingVehicle(BookingPojo bookingPojo) throws ParseException {

        BookingEntity bookingEntity = new BookingEntity();
        SlotsEntity slotsEntity = new SlotsEntity();

        boolean vehicleAvailability = slotsRepo.findVehicleAvailabilityOnRequiredDate(bookingPojo.getVehicleNumber(), bookingPojo.getFromDate(), bookingPojo.getToDate());
        if (!vehicleAvailability) {
            saveUser(bookingPojo);
            saveBooking(bookingEntity, bookingPojo);
            saveSlot(slotsEntity, bookingEntity);
        } else {
            return "Slots already Booked";
        }
        return "Booking Successful";
    }

    private void saveBooking(BookingEntity bookingEntity, BookingPojo bookingPojo) {
        bookingEntity.setVehicleNumber(bookingPojo.getVehicleNumber());
        bookingEntity.setFromDate(bookingPojo.getFromDate());
        bookingEntity.setToDate(bookingPojo.getToDate());
        bookingEntity.setBookingId(generateBookingId());
        bookingEntity.setMobile(bookingPojo.getUserPojo().getMobile());
        bookingEntity.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());
        bookingEntity.setBookingDate(format.format(new Date()));
        bookingRepo.save(bookingEntity);
    }

    private void saveSlot(SlotsEntity slotsEntity, BookingEntity bookingEntity) {
        slotsEntity.setIsAvailable(false);
        slotsEntity.setFromDate(bookingEntity.getFromDate());
        slotsEntity.setToDate(bookingEntity.getToDate());
        slotsEntity.setVehicleNumber(bookingEntity.getVehicleNumber());
        slotsEntity.setBookingId(bookingEntity.getBookingId());
        slotsRepo.save(slotsEntity);
    }

    private void saveUser(BookingPojo bookingPojo) {
        UserEntity user = userRepo.getUserByMobileNumber(bookingPojo.getUserPojo().getMobile());
        if (user == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setName(bookingPojo.getUserPojo().getName());
            userEntity.setEmail(bookingPojo.getUserPojo().getEmail());
            userEntity.setMobile(bookingPojo.getUserPojo().getMobile());
            userRepo.save(userEntity);
        }
    }

    private String generateId() {
        UUID uuid = UUID.randomUUID();
        return "NT" + uuid;
    }

    private String generateBookingId() {
        String str = generateId();
        return str.substring(0, 6);
    }

    @Override
    public BookingDetails getBookingDetails(String bookingId) {
        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        if (bookingEntity == null) {
            throw new BookingException(ResStatus.BOOKING_ID_NOT_FOUND);
        }
        BookingDetails bookingDetails = new BookingDetails();
        getUser(bookingEntity, bookingDetails);
        getSlot(bookingId, bookingDetails);
        getVehicleDetails(bookingDetails);
        return bookingDetails;
    }

    @Override
    public String confirmBooking(String bookingId) {
        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        bookingEntity.setBookingStatus(BookingStatusEnum.CONFIRMED.getCode());
        bookingRepo.save(bookingEntity);
        return "Booking is Confirmed";
    }

    @Override
    public String declineBooking(String bookingId) {
        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        bookingEntity.setBookingStatus(BookingStatusEnum.DECLINED.getCode());
        bookingRepo.save(bookingEntity);
        return "Booking is Declined";
    }

    @Override
    public VehicleBooked getBookedSlotsByVehicleNumber(String vehicleNumber) {
        VehicleBooked vehicleBooked = new VehicleBooked();
        Slots slots = new Slots();
        List<BookedDates> bookedDatesList = new ArrayList<>();

        try {
            List<SlotsEntity> slotsEntityList = slotsRepo.getByVehicleNUmber(vehicleNumber);

            for (SlotsEntity slotsEntity : slotsEntityList) {
                bookedDatesList.add(new BookedDates(slotsEntity.getFromDate(), Boolean.TRUE));

                List<String> inBetweenDates = generateInBetweenDates(slotsEntity.getFromDate(), slotsEntity.getToDate());
                for (String date : inBetweenDates) {
                    bookedDatesList.add(new BookedDates(date, Boolean.TRUE));
                }

                bookedDatesList.add(new BookedDates(slotsEntity.getToDate(), Boolean.TRUE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        slots.setVehicleNumber(vehicleNumber);
        slots.setDates(bookedDatesList);
        vehicleBooked.setSlots(slots);
        return vehicleBooked;
    }




    private List<String> generateInBetweenDates(String fromDate, String toDate) {
        List<String> inBetweenDates = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date startDate = sdf.parse(fromDate);
            Date endDate = sdf.parse(toDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            while (calendar.getTime().before(endDate)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Date currentDate = calendar.getTime();

                if (!currentDate.equals(endDate)) {
                    inBetweenDates.add(sdf.format(currentDate));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return inBetweenDates;
    }

    private void getVehicleDetails(BookingDetails bookingDetails) {
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(bookingDetails.getSlotsPojo().getVehicleNumber());
        VehiclePojo vehiclePojo = new VehiclePojo();
        vehiclePojo.setVehicleNumber(vehicleEntity.getVehicleNumber());
        vehiclePojo.setSeatCapacity(vehicleEntity.getSeatCapacity());
        vehiclePojo.setIsVehicleSleeper(vehicleEntity.getIsVehicleSleeper());
        vehiclePojo.setIsVehicleAC(vehicleEntity.getIsVehicleAC());
        vehiclePojo.setImageUrl(vehicleEntity.getS3ImageUrl());
        bookingDetails.setVehiclePojo(vehiclePojo);
    }

    private void getSlot(String bookingId, BookingDetails bookingDetails) {
        SlotsEntity slotsEntity = slotsRepo.findByBookingId(bookingId);
        SlotsPojo slotsPojo = new SlotsPojo();
        slotsPojo.setVehicleNumber(slotsEntity.getVehicleNumber());
        slotsPojo.setFromDate(slotsEntity.getFromDate());
        slotsPojo.setToDate(slotsEntity.getToDate());
        bookingDetails.setSlotsPojo(slotsPojo);
    }

    private void getUser(BookingEntity bookingEntity, BookingDetails bookingDetails) {
        UserEntity user = userRepo.getUserByMobileNumber(bookingEntity.getMobile());
        UserPojo userPojo = new UserPojo();
        userPojo.setMobile(user.getMobile());
        userPojo.setName(user.getName());
        userPojo.setEmail(user.getEmail());
        bookingDetails.setUserPojo(userPojo);
    }

}
