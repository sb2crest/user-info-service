package com.user.info.service.dto;

import lombok.Data;

@Data
public class PaymentResponse {

    private String status;

    private String message;

    private String razorPayOrderId;

    private String paymentDate;

}
