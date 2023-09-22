package com.example.user_info_service.service;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.SlotsEntity;
import com.example.user_info_service.entity.UserEntity;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.exception.BookingStatusEnum;
import com.example.user_info_service.exception.NumberException;
import com.example.user_info_service.pojo.BookingPojo;
import com.example.user_info_service.pojo.SlotsPojo;
import com.example.user_info_service.pojo.UserPojo;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.SlotsRepo;
import com.example.user_info_service.repository.UserRepo;
import com.example.user_info_service.repository.VehicleInfoRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.ParseException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class BookingServiceImplTest {

    @InjectMocks
    BookingServiceImpl bookingService;

    @Mock
    UserRepo userRepo;
    @Mock
    SlotsRepo slotsRepo;
    @Mock
    BookingRepo bookingRepo;
    @Mock
    VehicleInfoRepo vehicleInfoRepo;

    @Test
    void bookingVehicle() throws ParseException {
        ArgumentCaptor<String> vehicleNumberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toDateCaptor = ArgumentCaptor.forClass(String.class);

        when(slotsRepo.findVehicleAvailabilityOnRequiredDate(
                vehicleNumberCaptor.capture(),
                fromDateCaptor.capture(),
                toDateCaptor.capture())
        ).thenReturn(false);
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        assertEquals("Booking Successful", bookingService.bookingVehicle(createBookingPojo()));

    }

    @Test
    void bookingVehicleWhenUserDataAlreadyExist() throws ParseException {
        ArgumentCaptor<String> vehicleNumberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toDateCaptor = ArgumentCaptor.forClass(String.class);

        when(slotsRepo.findVehicleAvailabilityOnRequiredDate(
                vehicleNumberCaptor.capture(),
                fromDateCaptor.capture(),
                toDateCaptor.capture())
        ).thenReturn(false);
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(null);
        assertEquals("Booking Successful", bookingService.bookingVehicle(createBookingPojo()));

    }

    @Test
    void bookingVehicleWhenVehicleIsAlreadyBooked() throws ParseException {
        ArgumentCaptor<String> vehicleNumberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toDateCaptor = ArgumentCaptor.forClass(String.class);

        when(slotsRepo.findVehicleAvailabilityOnRequiredDate(
                vehicleNumberCaptor.capture(),
                fromDateCaptor.capture(),
                toDateCaptor.capture())
        ).thenReturn(true);
        assertEquals("Slots already Booked", bookingService.bookingVehicle(createBookingPojo()));

    }

    @Test
    void getBookingDetails() {
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        when(slotsRepo.findByBookingId(Mockito.anyString())).thenReturn(getSlotEntity());
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(getVehicleEntity());
        when(bookingRepo.getByBookingId(Mockito.anyString())).thenReturn(getBookingEntity());
        assertNotNull(bookingService.getBookingDetails("123"));
    }

    @Test
    void getBookingDetailsWhenNoMatchingDetailsFoundForBookingId() {
        when(bookingRepo.getByBookingId(Mockito.anyString())).thenReturn(null);
        assertThrows(NumberException.class, () -> bookingService.getBookingDetails("123"));
    }

    @Test
    void testConfirmBooking() {
        BookingEntity bookingEntity = getBookingEntity();
        when(bookingRepo.getByBookingId(Mockito.anyString())).thenReturn(bookingEntity);
        String result = bookingService.confirmBooking("123");
        assertEquals(BookingStatusEnum.CONFIRMED.getCode(), bookingEntity.getBookingStatus());
        verify(bookingRepo, times(1)).save(bookingEntity);
        assertEquals("Booking is Confirmed", result);
    }

    @Test
    void testDeclineBooking() {
        BookingEntity bookingEntity = getBookingEntity();
        when(bookingRepo.getByBookingId(Mockito.anyString())).thenReturn(bookingEntity);
        String result = bookingService.declineBooking("123");
        assertEquals(BookingStatusEnum.DECLINED.getCode(), bookingEntity.getBookingStatus());
        verify(bookingRepo, times(1)).save(bookingEntity);
        assertEquals("Booking is Declined", result);
    }


    SlotsEntity getSlotEntity() {
        SlotsEntity slotsEntity = new SlotsEntity();
        slotsEntity.setBookingId("123");
        slotsEntity.setVehicleNumber("ka02m1234");
        slotsEntity.setId(1L);
        slotsEntity.setBookingEntity(null);
        return slotsEntity;
    }

    VehicleEntity getVehicleEntity() {
        VehicleEntity vehicleEntity = new VehicleEntity();
        vehicleEntity.setVehicleNumber("1234");
        vehicleEntity.setSeatCapacity(12);
        vehicleEntity.setIsVehicleAC(true);
        vehicleEntity.setS3ImageUrl("http/image");
        vehicleEntity.setIsVehicleSleeper(true);
        vehicleEntity.setVId(1L);
        return vehicleEntity;
    }

    UserEntity getUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setName("abc");
        userEntity.setBookingEntity(new ArrayList<>());
        userEntity.setId(1L);
        userEntity.setMobile("1234455667");
        return userEntity;
    }

    BookingEntity getBookingEntity() {
        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setBookingId("123");
        bookingEntity.setMobile("1234");
        bookingEntity.setId(1L);
        bookingEntity.setUserEntity(null);
        bookingEntity.setVehicleNumber("ka02m1234");
        bookingEntity.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());
        return bookingEntity;
    }

    private BookingPojo createBookingPojo() {
        BookingPojo bookingPojo = new BookingPojo();
        bookingPojo.setFromDate("12-02-2333");
        bookingPojo.setToDate("12-02-2333");
        bookingPojo.setVehicleNumber("ka02m1234");
        UserPojo userPojo = new UserPojo();
        userPojo.setMobile("1234456671");
        userPojo.setName("abc");
        bookingPojo.setUserPojo(userPojo);
        SlotsPojo slotsPojo = new SlotsPojo();
        slotsPojo.setVehicleNumber("ka02m1234");
        slotsPojo.setFromDate("12-02-2333");
        slotsPojo.setToDate("12-02-2333");
        slotsPojo.setIsAvailable(true);
        bookingPojo.setSlotsPojo(slotsPojo);

        return bookingPojo;
    }


}