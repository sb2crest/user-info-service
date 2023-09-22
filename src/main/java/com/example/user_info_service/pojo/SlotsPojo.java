package com.example.user_info_service.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlotsPojo {

    private String vehicleNumber;

    private String fromDate;

    private String toDate;

    private Boolean isAvailable;
}
