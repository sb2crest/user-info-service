package com.example.user_info_service.service;

import com.example.user_info_service.dto.PaymentDto;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    private RazorpayClient razorPayClient;

    @Mock
    private BookingRepo bookingRepo;

    @BeforeEach
    void setUp() throws RazorpayException {
        ReflectionTestUtils.setField(paymentService, "keyID", "123");
        ReflectionTestUtils.setField(paymentService, "keySecret", "12345");

        MockitoAnnotations.openMocks(this);

        // Initialize razorPayClient
        RazorpayClient razorPayClient = new RazorpayClient("your_api_key_id", "your_api_key_secret");
        paymentService = new PaymentServiceImpl(razorPayClient);
    }

//    @Test
//    void createPayment() throws JSONException, RazorpayException {
//        PaymentPojo paymentPojo = getPaymentPojo();
//        when(bookingRepo.validateUsingIdAndMobile(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
//
//        JSONObject orderRequest = new JSONObject();
//        orderRequest.put("amount", paymentPojo.getAmount());
//        orderRequest.put("currency", "INR");
//        orderRequest.put("receipt", "payment_receipt_" + System.currentTimeMillis());
//
//        Order razorpayOrder = new Order(orderRequest);
//
//        when(razorPayClient.orders.create(Mockito.any())).thenReturn(razorpayOrder);
//
//        PaymentResponse response = paymentService.createPayment(paymentPojo);
//
//        assertEquals("success", response.getStatus());
//        assertEquals("Payment created successfully", response.getMessage());
//        assertEquals(paymentPojo.getBookingId(), response.getRazorpayPaymentId());
//    }

    private PaymentDto getPaymentPojo() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setBookingId("NB34ye");
        paymentDto.setAmount(400);
        paymentDto.setMobile("123456789");
        return paymentDto;
    }

    public RazorpayClient getRazorpayClient() {
        return razorPayClient;
    }

}
