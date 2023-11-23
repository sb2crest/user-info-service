package com.example.user_info_service.service;

import com.example.user_info_service.dto.BookingResponse;
import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.PaymentEntity;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.dto.PaymentData;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepo bookingRepo;

    @Mock
    private RazorpayClient razorpayClient;

    @BeforeEach
    public void setup() throws RazorpayException {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(paymentService,"keyID","anjds123");
        ReflectionTestUtils.setField(paymentService,"keySecret","anhjsd123");

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
        return paymentEntity;
    }

    PaymentData getPaymentData(){
        PaymentData paymentData = new PaymentData();
        paymentData.setRazorPayOrderId("order123");
        paymentData.setRazorPayPaymentId("payment123");
        paymentData.setRazorPaySignature("56569a026e455f6946d76dad150e0e6b18e0e21719a599fd90f0d48852614625");
        return paymentData;
    }

    private Order getMockedRazorpayOrder() throws JSONException {
        JSONObject orderRequest = new JSONObject();
        // Customize this order to match the expected behavior in your test case
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
        return bookingEntity;
    }
}
