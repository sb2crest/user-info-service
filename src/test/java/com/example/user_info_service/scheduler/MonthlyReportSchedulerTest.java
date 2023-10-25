package com.example.user_info_service.scheduler;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.model.EmailTransport;
import com.example.user_info_service.repository.BookingRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
class MonthlyReportSchedulerTest {

    @InjectMocks
    private MonthlyReportScheduler scheduler;

    @Mock
    private BookingRepo bookingRepo;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailTransport emailTransport;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        scheduler = new MonthlyReportScheduler(emailTransport, bookingRepo);

        ReflectionTestUtils.setField(scheduler, "emailUsername", "user@gmail.com");
        ReflectionTestUtils.setField(scheduler, "emailPassword", "password");
        ReflectionTestUtils.setField(scheduler, "toEmailAddress", "sender");
        ReflectionTestUtils.setField(scheduler, "mailHost", "host");
        ReflectionTestUtils.setField(scheduler, "mailPort", 587); // Replace with your actual mail port
        ReflectionTestUtils.setField(scheduler, "mailStartTlsRequired", true);
        ReflectionTestUtils.setField(scheduler, "mailStartTlsEnable", true);
        ReflectionTestUtils.setField(scheduler, "mailSocketFactoryClass", "socket");
        ReflectionTestUtils.setField(scheduler, "mailDebug", true);
    }

    @Test
    void testSendWeeklyReportEmail() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        when(bookingRepo.getReportForWeeklyAndMonthly(any(),any())).thenReturn(List.of(getBookingEntity()));
        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(emailTransport).send(any(MimeMessage.class));
        scheduler.sendMonthlyReportEmail();
        verify(emailTransport,times(1)).send(any());
    }


    @Test
    public void sendMonthlyReportEmailWhenThereWereNoBooking() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        when(bookingRepo.getReportForWeeklyAndMonthly(any(),any())).thenReturn(new ArrayList<>());
        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(emailTransport).send(any(MimeMessage.class));
        scheduler.sendMonthlyReportEmail();
        verify(emailTransport,times(1)).send(any());
    }

    BookingEntity getBookingEntity() {
        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setBookingId("123");
        bookingEntity.setMobile("1234");
        bookingEntity.setId(1L);
        bookingEntity.setUserEntity(null);
        bookingEntity.setVehicleNumber("ka02m1234");
        bookingEntity.setBookingDate(LocalDate.now());
        bookingEntity.setFromDate(LocalDate.now().minusDays(3));
        bookingEntity.setToDate(LocalDate.now().minusDays(1));
        bookingEntity.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());
        return bookingEntity;
    }
}