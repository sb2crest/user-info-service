package com.example.user_info_service.service;

import com.example.user_info_service.pojo.PaymentPojo;
import com.example.user_info_service.pojo.PaymentResponse;
import com.razorpay.RazorpayException;

public interface PaymentService {
    PaymentResponse createPayment(PaymentPojo paymentPojo) throws RazorpayException;

    String generateRazorpaySignature(String razorpayPaymentId, String bookingId);

    boolean verifyRazorpaySignature(String razorpayPaymentId, String bookingId, String signature);
}
