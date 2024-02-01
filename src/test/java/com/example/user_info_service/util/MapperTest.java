package com.example.user_info_service.util;

import com.example.user_info_service.dto.*;
import com.example.user_info_service.entity.*;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.repository.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class MapperTest {

    @InjectMocks
    private Mapper mapper;

    @InjectMocks
    private Validation validation;

    @Mock
    private BookingRepo bookingRepo;

    @Mock
    private SlotsRepo slotsRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    VehicleInfoRepo vehicleInfoRepo;


    @Test
    void testSaveBooking(){
        mapper.saveBooking(new BookingEntity(),createBookingPojo());
    }

    @Test
    void testSaveUser(){
        when(userRepo.getUserByMobileNumber(anyString())).thenReturn(getUserEntity());
        mapper.saveUser(createBookingPojo());
    }

    @Test
    void testSaveUserIsNull(){
        when(userRepo.getUserByMobileNumber(anyString())).thenReturn(null);
        mapper.saveUser(createBookingPojo());
    }

    @Test
    void testSaveSlot(){
        mapper.saveSlot(new BookingEntity());
    }

    @Test
    void testGetBookingData(){
        when(paymentRepository.findByBookingId(anyString())).thenReturn(List.of(getPaymentEntity()));
        mapper.getBookingData(new BookingDetails(),getBookingEntity());
    }

    @Test
    void testGetBookingDataWhenStatusIsCompleted(){
        BookingEntity bookingEntity = getBookingEntity();
        bookingEntity.setBookingStatus("C");
        when(paymentRepository.findByBookingId(anyString())).thenReturn(List.of(getPaymentEntity()));
        mapper.getBookingData(new BookingDetails(),bookingEntity);
    }

    @Test
    void testGetBookingDataWhenStatusIsNotBooked(){
        BookingEntity bookingEntity = getBookingEntity();
        bookingEntity.setBookingStatus("a");
        when(paymentRepository.findByBookingId(anyString())).thenReturn(List.of(getPaymentEntity()));
        mapper.getBookingData(new BookingDetails(),bookingEntity);
    }

    @Test
    void testGetAmount(){
        BookingEntity bookingEntity = getBookingEntity();
        when(paymentRepository.findByBookingId(anyString())).thenReturn(List.of(getPaymentEntity()));
        Assertions.assertEquals(100.0, mapper.getAmount(bookingEntity));
    }

    @Test
    void testGetAmountForCompleted(){
        BookingEntity bookingEntity = getBookingEntity();
        bookingEntity.setBookingStatus("C");
        when(paymentRepository.findByBookingId(anyString())).thenReturn(List.of(getPaymentEntity()));
        Assertions.assertEquals(100.0, mapper.getAmount(bookingEntity));
    }

    @Test
    void testGetAmountWhenPaymentIsNull(){
        BookingEntity bookingEntity = getBookingEntity();
        bookingEntity.setBookingStatus("C");
        when(paymentRepository.findByBookingId(anyString())).thenReturn(null);
        Assertions.assertThrows(BookingException.class, ()->mapper.getAmount(bookingEntity));
    }

    @Test
    void testGetAmountWhenPaymentIsEmpty(){
        BookingEntity bookingEntity = getBookingEntity();
        bookingEntity.setBookingStatus("C");
        when(paymentRepository.findByBookingId(anyString())).thenReturn(List.of());
        Assertions.assertThrows(BookingException.class, ()->mapper.getAmount(bookingEntity));
    }

    @Test
    void testGetUser(){
        when(userRepo.getUserByMobileNumber(anyString())).thenReturn(getUserEntity());
        mapper.getUser(getBookingEntity(),new BookingDetails());
    }
    @Test
    void testGetUserWhenUserDataIsNull(){
        when(userRepo.getUserByMobileNumber(anyString())).thenReturn(null);
        Assertions.assertThrows(BookingException.class,()->mapper.getUser(getBookingEntity(),new BookingDetails()));
    }


    @Test
    void testGetBookingInfoWhenStatusIsBooked(){
        when(paymentRepository.findByBookingId(anyString())).thenReturn(List.of(getPaymentEntity()));
        mapper.getBookingInfo(getVehicleEntity(),getBookingEntity());
    }

    @Test
    void testGetBookingInfoWhenStatusIsCompleted(){
        BookingEntity bookingEntity = getBookingEntity();
        bookingEntity.setBookingStatus("C");
        when(paymentRepository.findByBookingId(anyString())).thenReturn(List.of(getPaymentEntity()));
        mapper.getBookingInfo(getVehicleEntity(),bookingEntity);
    }

    @Test
    void testGetBookingInfoWhenStatusIsEnquiry(){
        BookingEntity bookingEntity = getBookingEntity();
        bookingEntity.setBookingStatus("E");
        when(paymentRepository.findByBookingId(anyString())).thenReturn(List.of(getPaymentEntity()));
        mapper.getBookingInfo(getVehicleEntity(),bookingEntity);
    }

    @Test
    void testGetVehicleDetails(){
        when(vehicleInfoRepo.getByVehicleNumber(any())).thenReturn(getVehicleEntity());
        mapper.getVehicleDetails(new BookingDetails(),"123");
    }

    @Test
    void testGetVehicleDetailsWhenVehicleDataIsNull(){
        when(vehicleInfoRepo.getByVehicleNumber(any())).thenReturn(null);
        Assertions.assertThrows(BookingException.class,()->mapper.getVehicleDetails(new BookingDetails(),"123"));
    }

    @Test
    void testGetVehiclePojo(){
        mapper.getVehiclePojo(new DestinationResponse(),getVehicleEntity());
    }

    @Test
    void testGetVehicleFilterDetailsWhenFilterIsNull(){
        VehicleEntity vehicleEntity = getVehicleEntity();
        vehicleEntity.setFilter(null);
        mapper.getVehicleFilterDetails(new VehicleDto(),vehicleEntity);
    }

    @Test
    void testGetDistanceRequestDetails(){
        mapper.getDistanceRequestDetails(getVehiclesAvailable(),List.of("123","456"));
    }

    @Test
    void testGenerateInBetweenDates(){
        mapper.generateInBetweenDates(LocalDate.now(),LocalDate.now().plusDays(2));
    }

    private BookingDto createBookingPojo() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setFromDate(LocalDate.now());
        bookingDto.setToDate(LocalDate.now());
        bookingDto.setVehicleNumber("ka02m1234");
        UserDto userDto = new UserDto();
        userDto.setMobile("1234456671");
        userDto.setFirstName("abc");
        userDto.setMiddleName("abc");
        userDto.setMiddleName("abc");
        userDto.setEmail("abc@gmail.com");
        bookingDto.setUser(userDto);
        SlotsDto slotsDto = new SlotsDto();
        slotsDto.setVehicleNumber("ka02m1234");
        slotsDto.setFromDate("12-02-2333");
        slotsDto.setToDate("12-02-2333");
        bookingDto.setSlots(slotsDto);

        return bookingDto;
    }

    BookingEntity getBookingEntity() {
        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setBookingId("123");
        bookingEntity.setMobile("1234");
        bookingEntity.setId(1L);
        bookingEntity.setUserEntity(null);
        bookingEntity.setAdvanceAmountPaid(20.00);
        bookingEntity.setTotalAmount(200.00);
        bookingEntity.setVehicleNumber("ka02m1234");
        bookingEntity.setFromDate(LocalDate.now().minusDays(3));
        bookingEntity.setToDate(LocalDate.now());
        bookingEntity.setBookingDate(LocalDateTime.now().minusWeeks(1));
        bookingEntity.setBookingStatus(BookingStatusEnum.BOOKED.getCode());
        UserEntity user = new UserEntity();
        user.setLastName("Thalapathy");
        user.setFirstName("Vijay");
        user.setMobile("1234");
        PaymentEntity paymentEntity = getPaymentEntity();
        bookingEntity.setUserEntity(user);
        bookingEntity.setPaymentEntities(List.of(paymentEntity));
        return bookingEntity;
    }

    PaymentEntity getPaymentEntity(){
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setAmount(100.00);
        return paymentEntity;
    }

    UserEntity getUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("abc");
        userEntity.setMiddleName("abc");
        userEntity.setLastName("abc");
        userEntity.setBookingEntity(new ArrayList<>());
        userEntity.setId(1L);
        userEntity.setMobile("1234455667");
        return userEntity;
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
        vehicleEntity.setFilter("FS/AC");
        vehicleEntity.setS3ImageUrl(List.of("http/image"));
        vehicleEntity.setVId(1L);
        return vehicleEntity;
    }

    VehiclesAvailable getVehiclesAvailable() {

        List<String> vehicleNumbersList = new ArrayList<>();
        vehicleNumbersList.add("KA01HJ1234");

        DistanceRequest distanceRequest = new DistanceRequest();
        distanceRequest.setSource("ABCV");
        distanceRequest.setDestination("bvcs");
        distanceRequest.setVehicleNumbers(vehicleNumbersList);
        distanceRequest.setSourceLatitude(17.827729);
        distanceRequest.setSourceLongitude(77.3762781);
        distanceRequest.setDestinationLatitude(17.647389);
        distanceRequest.setDestinationLongitude(87.737829);
        distanceRequest.setMultipleDestination(false);

        VehiclesAvailable vehiclesAvailable = new VehiclesAvailable("12-11-2023","15-11-2023");
        vehiclesAvailable.setFilter("AC/FS");
        vehiclesAvailable.setDistanceRequest(distanceRequest);
        return vehiclesAvailable;
    }
}