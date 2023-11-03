package com.example.user_info_service.dto;

import lombok.Data;

@Data
public class PaymentDto {

    private String bookingId;

    private String mobile;

    private double amount;

}
