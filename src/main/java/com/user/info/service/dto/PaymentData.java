package com.user.info.service.dto;

import lombok.Data;

@Data
public class PaymentData {

    private String razorPayOrderId;

    private String razorPayPaymentId;

    private String razorPaySignature;

    private String paymentStatus;

}
