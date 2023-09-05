package com.example.user_info_service.pojo;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@Data
public class SlotsPojo {

    private String vehicleNumber;

    private String fromDate;

    private String toDate;

    private Boolean isAvailable;
}
