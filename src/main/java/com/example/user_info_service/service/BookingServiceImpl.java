package com.example.user_info_service.service;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.SlotsEntity;
import com.example.user_info_service.entity.UserEntity;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.model.GmailValidator;
import com.example.user_info_service.pojo.*;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.SlotsRepo;
import com.example.user_info_service.repository.UserRepo;
import com.example.user_info_service.repository.VehicleInfoRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
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

    private final DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    @Transactional
    public String bookingVehicle(BookingPojo bookingPojo) throws ParseException {
        checkUserDetails(bookingPojo.getUserPojo());

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


    private void checkUserDetails(UserPojo userPojo) {
        if (userPojo.getMobile().isEmpty()) {
            throw new BookingException(ResStatus.ENTER_NUMBER);
        }
        if (userPojo.getMobile().length() != 10) {
            throw new BookingException(ResStatus.MOBILE_DIGIT);
        }
        if (userPojo.getEmail() != null && !GmailValidator.isValidGmail(userPojo.getEmail())) {
            throw new BookingException(ResStatus.INVALID_EMAIL);
        }
    }

    private void saveBooking(BookingEntity bookingEntity, BookingPojo bookingPojo) {
        bookingEntity.setVehicleNumber(bookingPojo.getVehicleNumber());
        bookingEntity.setFromDate(bookingPojo.getFromDate());
        bookingEntity.setToDate(bookingPojo.getToDate());
        bookingEntity.setBookingId(generateBookingId());
        bookingEntity.setMobile(bookingPojo.getUserPojo().getMobile());
        bookingEntity.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());
        bookingEntity.setBookingDate(LocalDate.now());
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
        return "NB" + uuid;
    }

    private String generateBookingId() {
        String str = generateId();
        return str.substring(0, 6);
    }

    @Override
    public BookingDetails getBookingDetails(String bookingId) {
        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        validateBookingEntity(bookingEntity);
        BookingDetails bookingDetails = new BookingDetails();
        getUser(bookingEntity, bookingDetails);
        getSlot(bookingId, bookingDetails);
        getVehicleDetails(bookingDetails);
        return bookingDetails;
    }

    @Override
    public BookingInfo getBookingInfoByBookingId(String bookingId) {

        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        validateBookingEntity(bookingEntity);
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(bookingEntity.getVehicleNumber());

        return getBookingInfo(vehicleEntity, bookingEntity);
    }

    private BookingInfo getBookingInfo(VehicleEntity vehicleEntity, BookingEntity bookingEntity) {
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setVehicleNumber(bookingEntity.getVehicleNumber());
        bookingInfo.setToDate(localDateFormat.format(bookingEntity.getToDate()));
        bookingInfo.setFromDate(localDateFormat .format(bookingEntity.getFromDate()));
        bookingInfo.setBookingDate(bookingEntity.getBookingDate());
        bookingInfo.setDriverName(vehicleEntity.getDriverName());
        bookingInfo.setDriverNumber(vehicleEntity.getDriverNumber());
        bookingInfo.setAlternateNumber(vehicleEntity.getAlternateNumber());
        return bookingInfo;
    }

    @Override
    public String confirmBooking(String bookingId) {
        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        validateBookingEntity(bookingEntity);
        bookingEntity.setBookingStatus(BookingStatusEnum.CONFIRMED.getCode());
        bookingRepo.save(bookingEntity);
        return "Booking is Confirmed";
    }

    @Override
    public String declineBooking(String bookingId) {
        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        validateBookingEntity(bookingEntity);
        bookingEntity.setBookingStatus(BookingStatusEnum.DECLINED.getCode());
        bookingRepo.save(bookingEntity);
        return "Booking is Declined";
    }

    @Override
    public VehicleBooked getBookedSlotsByVehicleNumber(String vehicleNumber) {
        VehicleBooked vehicleBooked = new VehicleBooked();
        Slots slots = new Slots();
        List<BookedDates> bookedDatesList = new ArrayList<>();

        List<SlotsEntity> slotsEntityList = slotsRepo.getByVehicleNUmber(vehicleNumber);

        for (SlotsEntity slotsEntity : slotsEntityList) {
            bookedDatesList.add(new BookedDates(slotsEntity.getFromDate(), Boolean.TRUE));

            List<LocalDate> inBetweenDates = generateInBetweenDates(slotsEntity.getFromDate(), slotsEntity.getToDate());
            for (LocalDate date : inBetweenDates) {
                bookedDatesList.add(new BookedDates(date, Boolean.TRUE));
            }

            bookedDatesList.add(new BookedDates(slotsEntity.getToDate(), Boolean.TRUE));
        }

        slots.setVehicleNumber(vehicleNumber);
        slots.setDates(bookedDatesList);
        vehicleBooked.setSlots(slots);
        return vehicleBooked;
    }

    @Override
    public List<VehiclePojo> getVehicleAvailability(VehiclesAvailable vehiclesAvailable) {
        List<VehiclePojo> vehiclePojos = new ArrayList<>();
        List<String> unavailableVehicleList = slotsRepo.getUnavailableList(vehiclesAvailable.getFromDate(), vehiclesAvailable.getToDate());
        List<VehicleEntity> vehicleEntities = vehicleInfoRepo.getAvailableVehicle(unavailableVehicleList, vehiclesAvailable.getIsAC(), vehiclesAvailable.getIsSleeper());
        getVehiclePojo(vehiclePojos, vehicleEntities);
        return vehiclePojos;

    }

    private void getVehiclePojo(List<VehiclePojo> vehiclePojos, List<VehicleEntity> vehicleEntities) {
        for (VehicleEntity vehicleEntity : vehicleEntities) {
            VehiclePojo vehiclePojo = new VehiclePojo();
            vehiclePojo.setSeatCapacity(vehicleEntity.getSeatCapacity());
            vehiclePojo.setVehicleNumber(vehicleEntity.getVehicleNumber());
            vehiclePojo.setImageUrl(vehicleEntity.getS3ImageUrl());
            vehiclePojo.setIsVehicleAC(vehicleEntity.getIsVehicleAC());
            vehiclePojo.setIsVehicleSleeper(vehicleEntity.getIsVehicleSleeper());

            vehiclePojos.add(vehiclePojo);
        }
    }

    private List<LocalDate> generateInBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> inBetweenDates = new ArrayList<>();

        LocalDate currentDate = startDate.plusDays(1);

        while (currentDate.isBefore(endDate)) {
            inBetweenDates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        return inBetweenDates;
    }

    private void getVehicleDetails(BookingDetails bookingDetails) {
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(bookingDetails.getSlotsPojo().getVehicleNumber());
        validateVehicleEntity(vehicleEntity);
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
        validateSlotEntity(slotsEntity);
        SlotsPojo slotsPojo = new SlotsPojo();
        slotsPojo.setVehicleNumber(slotsEntity.getVehicleNumber());
        slotsPojo.setFromDate(localDateFormat.format(slotsEntity.getFromDate()));
        slotsPojo.setToDate(localDateFormat.format(slotsEntity.getToDate()));
        bookingDetails.setSlotsPojo(slotsPojo);
    }

    private void getUser(BookingEntity bookingEntity, BookingDetails bookingDetails) {
        UserEntity user = userRepo.getUserByMobileNumber(bookingEntity.getMobile());
        validateUserEntity(user);
        UserPojo userPojo = new UserPojo();
        userPojo.setMobile(user.getMobile());
        userPojo.setName(user.getName());
        userPojo.setEmail(user.getEmail());
        bookingDetails.setUserPojo(userPojo);
    }

    private void validateBookingEntity(BookingEntity bookingEntity) {
        if (bookingEntity == null ) {
            throw new BookingException(ResStatus.BOOKING_NOT_FOUND);
        }
    }

    private void validateVehicleEntity(VehicleEntity vehicleEntity) {
        if (vehicleEntity == null ) {
            throw new BookingException(ResStatus.VEHICLE_NOT_FOUND);
        }
    }

    private void validateUserEntity(UserEntity userEntity) {
        if (userEntity == null ) {
            throw new BookingException(ResStatus.USER_NOT_FOUND);
        }
    }

    private void validateSlotEntity(SlotsEntity slotsEntity) {
        if (slotsEntity == null ) {
            throw new BookingException(ResStatus.SLOTS_NOT_FOUND);
        }
    }
}
