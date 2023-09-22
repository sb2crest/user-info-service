package com.example.user_info_service.pojo;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class BookingPojo {

    private String vehicleNumber;

    private String fromDate;

    private String toDate;

    private UserPojo userPojo;

    private SlotsPojo slotsPojo;
}
