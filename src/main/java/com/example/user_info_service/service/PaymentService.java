package com.example.user_info_service.service;

import com.example.user_info_service.dto.BookingResponse;
import com.example.user_info_service.dto.PaymentData;
import com.example.user_info_service.dto.PaymentDto;
import com.example.user_info_service.dto.PaymentResponse;
import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;

public interface PaymentService {
    PaymentResponse createPayment(PaymentDto paymentDto) throws RazorpayException;

    String generateRazorpaySignature(String razorpayPaymentId, String bookingId,String keySecret);

    ResponseEntity<BookingResponse> verifyRazorpaySignature(PaymentData paymentData);
}
