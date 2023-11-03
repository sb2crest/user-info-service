package com.example.user_info_service.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PaymentResponse {

    private String status;

    private String message;

    private String razorPayOrderId;

    private Date paymentDate;

}
