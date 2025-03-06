package com.user.info.service.service;

import com.user.info.service.dto.BookingResponse;
import com.user.info.service.dto.PaymentData;
import com.user.info.service.dto.PaymentDto;
import com.user.info.service.dto.PaymentResponse;
import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;

public interface PaymentService {
    PaymentResponse createPayment(PaymentDto paymentDto) throws RazorpayException;

    ResponseEntity<BookingResponse> verifyRazorpaySignature(PaymentData paymentData);
}
