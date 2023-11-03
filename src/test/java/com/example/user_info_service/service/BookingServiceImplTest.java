package com.example.user_info_service.service;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.SlotsEntity;
import com.example.user_info_service.entity.UserEntity;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.dto.*;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.SlotsRepo;
import com.example.user_info_service.repository.UserRepo;
import com.example.user_info_service.repository.VehicleInfoRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
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
    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    BookingEntity bookingEntity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(bookingService, "toEmailAddress", "sender");
    }

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
        BookingDto bookingDto = createBookingPojo();
        BookingResponse bookingResponse = bookingService.bookingVehicle(bookingDto);

        assertEquals(200 , bookingResponse.getStatusCode());
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
        BookingDto bookingDto = createBookingPojo();
        bookingDto.getUser().setMobile("");
        assertThrows(BookingException.class, () -> bookingService.bookingVehicle(bookingDto));

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
        BookingDto bookingDto = createBookingPojo();
        bookingDto.getUser().setMobile("1234");
        assertThrows(BookingException.class, () -> bookingService.bookingVehicle(bookingDto));

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
        BookingDto bookingDto = createBookingPojo();
        bookingDto.getUser().setEmail(null);
        String bookingId = bookingService.bookingVehicle(bookingDto).getBookingId();

        assertNotNull(bookingId);
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
        BookingDto bookingDto = createBookingPojo();
        bookingDto.getUser().setEmail("abc.com");
        assertThrows(BookingException.class, () -> bookingService.bookingVehicle(bookingDto));

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
        BookingDto bookingDto = createBookingPojo();
        String bookingId = bookingService.bookingVehicle(bookingDto).getBookingId();

        assertNotNull(bookingId);

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
        assertEquals(null, bookingService.bookingVehicle(createBookingPojo()).getBookingId());

    }

    @Test
    void getBookingDetails() {
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        when(slotsRepo.findByBookingId(Mockito.anyString())).thenReturn(getSlotEntity());
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(getVehicleEntity());
        when(bookingRepo.getByMobileNumber(Mockito.anyString())).thenReturn(List.of(getBookingEntity()));
        assertNotNull(bookingService.getBookingDetails("1234567890"));
    }

    @Test
    void getBookingDetailsWhenNoSlotDetailsFound() {
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        when(slotsRepo.findByBookingId(Mockito.anyString())).thenReturn(null);
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(getVehicleEntity());
        when(bookingRepo.getByMobileNumber(Mockito.anyString())).thenReturn(List.of(getBookingEntity()));
        assertThrows(BookingException.class , ()-> bookingService.getBookingDetails("1234567890"));
    }

    @Test
    void getBookingDetailsWhenNoBookingDetailsFound() {
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        when(slotsRepo.findByBookingId(Mockito.anyString())).thenReturn(null);
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(getVehicleEntity());
        when(bookingRepo.getByMobileNumber(Mockito.anyString())).thenReturn(List.of(getBookingEntity()));
        assertThrows(BookingException.class , ()-> bookingService.getBookingDetails("1234567890"));
    }

    @Test
    void getBookingDetailsWhenNoUserDetailsFound() {
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(null);
        when(slotsRepo.findByBookingId(Mockito.anyString())).thenReturn(getSlotEntity());
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(getVehicleEntity());
        when(bookingRepo.getByMobileNumber(Mockito.anyString())).thenReturn(List.of(getBookingEntity()));
        assertThrows(BookingException.class , ()-> bookingService.getBookingDetails("1234567890"));
    }

    @Test
    void getBookingDetailsWhenNoVehicleDetailsFound() {
        when(userRepo.getUserByMobileNumber(Mockito.anyString())).thenReturn(getUserEntity());
        when(slotsRepo.findByBookingId(Mockito.anyString())).thenReturn(getSlotEntity());
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(null);
        when(bookingRepo.getByMobileNumber(Mockito.anyString())).thenReturn(List.of(getBookingEntity()));
        assertThrows(BookingException.class , ()-> bookingService.getBookingDetails("1234567890"));
    }

    @Test
    void getBookingDetailsWhenNoMatchingDetailsFoundForMobileNumber() {
        when(bookingRepo.getByMobileNumber(Mockito.anyString())).thenReturn(null);
        assertThrows(BookingException.class, () -> bookingService.getBookingDetails("1234567890"));
    }

    @Test
    void testConfirmBooking() {
        BookingEntity bookingEntity = getBookingEntity();
        when(bookingRepo.getByBookingId(Mockito.anyString())).thenReturn(bookingEntity);
        String result = bookingService.confirmBooking("123");
        assertEquals(BookingStatusEnum.BOOKED.getCode(), bookingEntity.getBookingStatus());
        verify(bookingRepo, times(1)).save(bookingEntity);
        assertEquals("Booking is Confirmed", result);
    }

    @Test
    void testConfirmBookingWhenBookingEntityIsNull1() {
        when(bookingRepo.getByBookingId(Mockito.anyString())).thenReturn(null);
        assertThrows(BookingException.class, () -> bookingService.confirmBooking("123"));
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

    @Test
    void getBookedSlotsByVehicleNumberWhenNoSlotDetailsFound(){
        when(slotsRepo.getByVehicleNUmber(Mockito.anyString())).thenReturn(new ArrayList<>());
        assertThrows(BookingException.class, ()-> bookingService.getBookedSlotsByVehicleNumber("123"));
    }

    @Test
    void getBookingInfoByBookingIdTest(){
        when(bookingRepo.getByBookingId(Mockito.anyString())).thenReturn(getBookingEntity());
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(getVehicleEntity());
        BookingInfo bookingInfoByBookingId = bookingService.getBookingInfoByBookingId("123");
        assertEquals("ka02m1234" , bookingInfoByBookingId.getVehicleNumber());
    }

    @Test
    void testGetInTouch() throws Exception {
        UserData userData = new UserData();
        userData.setName("John Doe");
        userData.setEmail("johndoe@gmail.com");
        userData.setMessage("Hello, this is a test message.");

        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        bookingService.getInTouch(userData);
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
        userEntity.setFirstName("abc");
        userEntity.setMiddleName("abc");
        userEntity.setLastName("abc");
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
        bookingEntity.setFromDate(LocalDate.now().minusDays(3));
        bookingEntity.setToDate(LocalDate.now());
        bookingEntity.setBookingDate(LocalDate.now().minusWeeks(1));
        bookingEntity.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());
        return bookingEntity;
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

    private BookingResponse getBookingResponse(){
        BookingResponse  bookingResponse = new BookingResponse();
        bookingResponse.setBookingId("NBwh36");
        bookingResponse.setMessage("Booking Successful");
        bookingResponse.setStatusCode(200);

        return bookingResponse;

    }
}