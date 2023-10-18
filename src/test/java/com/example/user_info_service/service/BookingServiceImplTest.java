package com.example.user_info_service.service;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.SlotsEntity;
import com.example.user_info_service.entity.UserEntity;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.pojo.*;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        ArgumentCaptor<LocalDate> fromDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toDateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        when(slotsRepo.findVehicleAvailabilityOnRequiredDate(
                vehicleNumberCaptor.capture(),
                fromDateCaptor.capture(),
                toDateCaptor.capture())
        ).thenReturn(false);
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        assertEquals("Booking Successful", bookingService.bookingVehicle(createBookingPojo()));

    }

    @Test
    void bookingVehicleWithEmptyMobileNumber() throws ParseException {
        ArgumentCaptor<String> vehicleNumberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDate> fromDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toDateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        when(slotsRepo.findVehicleAvailabilityOnRequiredDate(
                vehicleNumberCaptor.capture(),
                fromDateCaptor.capture(),
                toDateCaptor.capture())
        ).thenReturn(false);
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        BookingPojo bookingPojo = createBookingPojo();
        bookingPojo.getUserPojo().setMobile("");
        assertThrows(BookingException.class, () -> bookingService.bookingVehicle(bookingPojo));

    }

    @Test
    void bookingVehicleWithInvalidMobileNumber() throws ParseException {
        ArgumentCaptor<String> vehicleNumberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDate> fromDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toDateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        when(slotsRepo.findVehicleAvailabilityOnRequiredDate(
                vehicleNumberCaptor.capture(),
                fromDateCaptor.capture(),
                toDateCaptor.capture())
        ).thenReturn(false);
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        BookingPojo bookingPojo = createBookingPojo();
        bookingPojo.getUserPojo().setMobile("1234");
        assertThrows(BookingException.class, () -> bookingService.bookingVehicle(bookingPojo));

    }

    @Test
    void bookingVehicleWhenEmailIsNull() throws ParseException {
        ArgumentCaptor<String> vehicleNumberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDate> fromDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toDateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        when(slotsRepo.findVehicleAvailabilityOnRequiredDate(
                vehicleNumberCaptor.capture(),
                fromDateCaptor.capture(),
                toDateCaptor.capture())
        ).thenReturn(false);
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        BookingPojo bookingPojo = createBookingPojo();
        bookingPojo.getUserPojo().setEmail(null);
        assertEquals("Booking Successful", bookingService.bookingVehicle(createBookingPojo()));

    }

    @Test
    void bookingVehicleWithInvalidEmail() throws ParseException {
        ArgumentCaptor<String> vehicleNumberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDate> fromDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toDateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        when(slotsRepo.findVehicleAvailabilityOnRequiredDate(
                vehicleNumberCaptor.capture(),
                fromDateCaptor.capture(),
                toDateCaptor.capture())
        ).thenReturn(false);
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        BookingPojo bookingPojo = createBookingPojo();
        bookingPojo.getUserPojo().setEmail("abc.com");
        assertThrows(BookingException.class, () -> bookingService.bookingVehicle(bookingPojo));

    }

    @Test
    void bookingVehicleWhenUserDataAlreadyExist() throws ParseException {
        ArgumentCaptor<String> vehicleNumberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDate> fromDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toDateCaptor = ArgumentCaptor.forClass(LocalDate.class);

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
        ArgumentCaptor<LocalDate> fromDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toDateCaptor = ArgumentCaptor.forClass(LocalDate.class);

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
        assertThrows(BookingException.class, () -> bookingService.getBookingDetails("123"));
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

    @Test
    void getVehicleAvailabilityTest() {
        List<VehicleEntity> vehicleEntities = List.of(getVehicleEntity());
        when(vehicleInfoRepo.getAvailableVehicle(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(vehicleEntities);
        when(slotsRepo.getUnavailableList(Mockito.any(), Mockito.any())).thenReturn(List.of("KA01AB1123","KA01AB2234"));
        assertEquals(12,bookingService.getVehicleAvailability(getVehiclesAvailable()).get(0).getSeatCapacity());

    }

    @Test
    void getBookedSlotsByVehicleNumberTest(){
        when(slotsRepo.getByVehicleNUmber(Mockito.anyString())).thenReturn(List.of(getSlotEntity()));
        VehicleBooked vehicleBooked = bookingService.getBookedSlotsByVehicleNumber("123");
        assertEquals(vehicleBooked.getSlots().getVehicleNumber(),"123");
    }

    VehiclesAvailable getVehiclesAvailable() {
        VehiclesAvailable vehiclesAvailable = new VehiclesAvailable();
        vehiclesAvailable.setFromDate(LocalDate.now().minusDays(2));
        vehiclesAvailable.setToDate(LocalDate.now());
        vehiclesAvailable.setIsAC(true);
        vehiclesAvailable.setIsSleeper(true);
        return vehiclesAvailable;
    }

    SlotsEntity getSlotEntity() {
        SlotsEntity slotsEntity = new SlotsEntity();
        slotsEntity.setBookingId("123");
        slotsEntity.setVehicleNumber("ka02m1234");
        slotsEntity.setId(1L);
        slotsEntity.setFromDate(LocalDate.now().minusDays(2));
        slotsEntity.setToDate(LocalDate.now());
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
        bookingPojo.setFromDate(LocalDate.now());
        bookingPojo.setToDate(LocalDate.now());
        bookingPojo.setVehicleNumber("ka02m1234");
        UserPojo userPojo = new UserPojo();
        userPojo.setMobile("1234456671");
        userPojo.setName("abc");
        userPojo.setEmail("abc@gmail.com");
        bookingPojo.setUserPojo(userPojo);
        SlotsPojo slotsPojo = new SlotsPojo();
        slotsPojo.setVehicleNumber("ka02m1234");
        slotsPojo.setFromDate("12-02-2333");
        slotsPojo.setToDate("12-02-2333");
        bookingPojo.setSlotsPojo(slotsPojo);

        return bookingPojo;
    }


}