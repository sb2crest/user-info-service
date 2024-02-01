package com.example.user_info_service.service;

import com.example.user_info_service.dto.BookingResponse;
import com.example.user_info_service.dto.PaymentDto;
import com.example.user_info_service.dto.PaymentResponse;
import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.PaymentEntity;
import com.example.user_info_service.entity.SlotsEntity;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.dto.PaymentData;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.PaymentRepository;
import com.example.user_info_service.repository.SlotsRepo;
import com.example.user_info_service.util.Mapper;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepo bookingRepo;

    @Mock
    private SlotsRepo slotsRepo;

    @Mock
    private Mapper mapper;

    @Mock
    private RazorpayClient razorpayClient;

    @BeforeEach
    public void setup() throws RazorpayException {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(paymentService,"keyID","anjds123");
        ReflectionTestUtils.setField(paymentService,"keySecret","anhjsd123");


    }

    @Test
    public void testCreatePaymentWhenThrowsException() {
        PaymentDto paymentDto = getPaymentDto();
        when(bookingRepo.validateUsingIdAndMobile(eq("booking123"), eq("1234567890"))).thenReturn(new BookingEntity());
        when(slotsRepo.findByBookingId(anyString())).thenReturn(null);
        PaymentResponse response = paymentService.createPayment(paymentDto);

        assertEquals("error", response.getStatus());
    }

    @Test
    public void testCreatePaymentWhenThrowsExceptionWhenSlotIsBooked() {
        PaymentDto paymentDto = getPaymentDto();
        when(bookingRepo.validateUsingIdAndMobile(eq("booking123"), eq("1234567890"))).thenReturn(new BookingEntity());
        when(slotsRepo.findByBookingId(anyString())).thenReturn(getSlotEntity());
        PaymentResponse response = paymentService.createPayment(paymentDto);

        assertEquals("error", response.getStatus());
    }

    @Test
    public void testCreatePaymentWhenThrowsExceptionWhenForRequiredDatePaymentIsAlreadyDone() {
        PaymentDto paymentDto = getPaymentDto();
        when(bookingRepo.validateUsingIdAndMobile(eq("booking123"), eq("1234567890"))).thenReturn(new BookingEntity());
        when(slotsRepo.findByBookingId(anyString())).thenReturn(null);
        when(paymentRepository.isSlotBooked(any(),any(),any())).thenReturn(getPaymentEntity());
        assertThrows(BookingException.class,()->paymentService.createPayment(paymentDto));
    }

    @Test
    public void testCreatePaymentWithInvalidBooking() {
        PaymentDto paymentDto = getPaymentDto();
        when(bookingRepo.validateUsingIdAndMobile(eq("booking123"), eq("1234567890"))).thenReturn(null);
        BookingException exception = assertThrows(BookingException.class, () -> paymentService.createPayment(paymentDto));
        assertEquals("5008: No record found with matching ID and Mobile Number : 500 INTERNAL_SERVER_ERROR", exception.getMessage());
    }


    @Test
    public void testVerifyRazorpaySignature_Success() {
        when(paymentRepository.findBookingIdByRazorPayOrderId(Mockito.anyString())).thenReturn(getPaymentEntity());
        when(bookingRepo.getByBookingId(Mockito.any())).thenReturn(getBookingEntity());
        ResponseEntity<BookingResponse> response = paymentService.verifyRazorpaySignature(getPaymentData());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testVerifyRazorpaySignature_Success_When_AdvanceAmountPaid() {
        BookingEntity bookingEntity = getBookingEntity();
        bookingEntity.setAdvanceAmountPaid(1000.00);
        when(paymentRepository.findBookingIdByRazorPayOrderId(Mockito.anyString())).thenReturn(getPaymentEntity());
        when(bookingRepo.getByBookingId(Mockito.any())).thenReturn(bookingEntity);
        ResponseEntity<BookingResponse> response = paymentService.verifyRazorpaySignature(getPaymentData());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testVerifyRazorpaySignature_InvalidSignature() {
        PaymentData paymentData = getPaymentData();
        paymentData.setRazorPaySignature("abc");
        when(paymentRepository.findBookingIdByRazorPayOrderId(Mockito.anyString())).thenReturn(getPaymentEntity());
        ResponseEntity<BookingResponse> response = paymentService.verifyRazorpaySignature(paymentData);
        assertNotNull(response);
        assertEquals(HttpStatus.PAYMENT_REQUIRED, response.getStatusCode());
    }

    @Test
    public void testVerifyRazorpaySignatureWhenPaymentEntityNotFound() {
        when(paymentRepository.findBookingIdByRazorPayOrderId(Mockito.anyString())).thenReturn(null);
        assertThrows(BookingException.class, () -> paymentService.verifyRazorpaySignature(getPaymentData()));
    }

    PaymentEntity getPaymentEntity(){
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setRazorPayOrderId("order123");
        paymentEntity.setPaymentStatus("SUCCESS");
        paymentEntity.setBookingId("123");
        paymentEntity.setAmount(1000.00);
        return paymentEntity;
    }

    PaymentData getPaymentData() {
        PaymentData paymentData = new PaymentData();
        paymentData.setRazorPayOrderId("order123");
        paymentData.setRazorPayPaymentId("payment123");
        paymentData.setRazorPaySignature("56569a026e455f6946d76dad150e0e6b18e0e21719a599fd90f0d48852614625");
        return paymentData;
    }

    private Order getMockedRazorpayOrder() throws JSONException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", 1000);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "payment_receipt_123456");
        orderRequest.put("id", "mockedOrder123");
        return new Order(orderRequest);
    }

    BookingEntity getBookingEntity() {
        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setBookingId("123");
        bookingEntity.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());
        bookingEntity.setTotalAmount(10000.00);
        return bookingEntity;
    }

    PaymentDto getPaymentDto() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setBookingId("booking123");
        paymentDto.setMobile("1234567890");
        paymentDto.setAmount(1000);

        return paymentDto;
    }

    SlotsEntity getSlotEntity() {
        SlotsEntity slotsEntity = new SlotsEntity();
        slotsEntity.setFromDate(LocalDate.now());
        slotsEntity.setToDate(LocalDate.now().plusDays(1));
        slotsEntity.setBookingId("NB12vsq");
        slotsEntity.setVehicleNumber("KA01HJ1234");
        slotsEntity.setIsAvailable(false);
        return slotsEntity;
    }
}
