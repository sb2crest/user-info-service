package com.example.user_info_service.scheduler;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.UserEntity;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.model.EmailTransport;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.VehicleInfoRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
class TomorrowsBookingTest {

    @InjectMocks
    private TomorrowsBooking tomorrowsBooking;

    @Mock
    private BookingRepo bookingRepo;

    @Mock
    private VehicleInfoRepo vehicleInfoRepo;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailTransport emailTransport;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(tomorrowsBooking, "toEmailAddress", "sender");
        ReflectionTestUtils.setField(tomorrowsBooking, "logo", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/1200px-Image_created_with_a_mobile_phone.png");

    }

    @Test
    void upcomingBookingsTest() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        when(bookingRepo.getTomorrowsBooking(any())).thenReturn(getBookingEntity());
        when(vehicleInfoRepo.getByVehicleNumbers(any())).thenReturn(getVehicleEntity());
        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(emailTransport).send(any(MimeMessage.class));
        tomorrowsBooking.tomorrowsBookingDetails();
        verify(bookingRepo , times(1)).getTomorrowsBooking(Mockito.any());

    }

    @Test
    void upcomingBookingsTest1() throws Exception {
        List<BookingEntity> bookingEntities = getBookingEntity();
        bookingEntities.get(0).getUserEntity().setEmail(null);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        when(bookingRepo.getTomorrowsBooking(any())).thenReturn(bookingEntities);
        when(vehicleInfoRepo.getByVehicleNumbers(any())).thenReturn(getVehicleEntity());
        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(emailTransport).send(any(MimeMessage.class));
        tomorrowsBooking.tomorrowsBookingDetails();
        verify(bookingRepo , times(1)).getTomorrowsBooking(Mockito.any());

    }

    @Test
    void upcomingBookingsWhenThereAreNoBookingTomorrow() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        when(bookingRepo.getTomorrowsBooking(any())).thenReturn(new ArrayList<>());
        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(emailTransport).send(any(MimeMessage.class));
        tomorrowsBooking.tomorrowsBookingDetails();
        verify(bookingRepo , times(1)).getTomorrowsBooking(Mockito.any());

    }

    List<BookingEntity> getBookingEntity() {
        List<BookingEntity> bookingEntityList = new ArrayList<>();
        BookingEntity booking = new BookingEntity();
        booking.setBookingId("123");
        booking.setMobile("1234");
        booking.setId(1L);
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("abc");
        userEntity.setMiddleName("abc");
        userEntity.setLastName("abc");
        userEntity.setMobile("1234");
        userEntity.setEmail("abc@gmail.com");
        booking.setUserEntity(userEntity);
        booking.setVehicleNumber("ka02m1234");
        booking.setBookingDate(LocalDate.now());
        booking.setFromDate(LocalDate.now().minusDays(3));
        booking.setToDate(LocalDate.now().minusDays(1));
        booking.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());

        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setBookingId("123");
        bookingEntity.setMobile("1234");
        bookingEntity.setId(1L);
        UserEntity user = new UserEntity();
        user.setFirstName("abc");
        user.setMiddleName("abc");
        user.setLastName("abc");
        user.setMobile("1234");
        user.setEmail("abc@gmail.com");
        bookingEntity.setUserEntity(user);
        bookingEntity.setVehicleNumber("ka02m1234");
        bookingEntity.setBookingDate(LocalDate.now());
        bookingEntity.setFromDate(LocalDate.now().minusDays(3));
        bookingEntity.setToDate(LocalDate.now().minusDays(1));
        bookingEntity.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());

        BookingEntity bookingEntity1 = new BookingEntity();
        bookingEntity1.setBookingId("123");
        bookingEntity1.setMobile("1234");
        bookingEntity1.setId(1L);
        UserEntity user1 = new UserEntity();
        user1.setFirstName("abc");
        user1.setMiddleName("abc");
        user1.setLastName("abc");
        user1.setMobile("1234");
        user1.setEmail("abc@gmail.com");
        bookingEntity1.setUserEntity(user1);
        bookingEntity1.setVehicleNumber("ka02m1234");
        bookingEntity1.setBookingDate(LocalDate.now());
        bookingEntity1.setFromDate(LocalDate.now().minusDays(3));
        bookingEntity1.setToDate(LocalDate.now().minusDays(1));
        bookingEntity1.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());

        bookingEntityList.add(booking);
        bookingEntityList.add(bookingEntity);
        bookingEntityList.add(bookingEntity1);
        return bookingEntityList;
    }

    List<VehicleEntity> getVehicleEntity() {
        List<VehicleEntity> vehicleEntities = new ArrayList<>();
        VehicleEntity vehicleEntity = new VehicleEntity();
        vehicleEntity.setVehicleNumber("ka02m1234");
        vehicleEntity.setSeatCapacity(12);
        vehicleEntity.setFilter("FS/AC");
        vehicleEntity.setS3ImageUrl(List.of("http/image"));
        vehicleEntity.setVId(1L);
        vehicleEntities.add(vehicleEntity);
        return vehicleEntities;
    }
}