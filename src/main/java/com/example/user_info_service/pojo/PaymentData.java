package com.example.user_info_service.pojo;

import lombok.Data;

@Data
public class PaymentData {

    private String razorPayOrderId;

    private String razorPayPaymentId;

    private String razorPaySignature;

    private String paymentStatus;

}
