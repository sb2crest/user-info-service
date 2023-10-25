package com.example.user_info_service.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "vehicle_info")
@Getter
@Setter
public class VehicleEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2405172041950251808L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long vId;

    @Column(name="seat_capacity")
    private Integer seatCapacity;

    @Column(name="vehicle_number")
    private String vehicleNumber;

    @Column(name = "file_url")
    private String s3ImageUrl;

    @Column(name = "is_ac")
    private Boolean isVehicleAC;

    @Column(name = "is_sleeper")
    private Boolean isVehicleSleeper;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_number")
    private String driverNumber;

    @Column(name = "alternative_number")
    private String alternateNumber;

    @Column(name = "emergency_contact_number")
    private String emergencyNumber;
}
