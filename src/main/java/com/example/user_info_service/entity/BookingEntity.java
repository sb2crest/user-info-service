package com.example.user_info_service.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "booking_info")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "vehicle_number", insertable = false, updatable = false)
    private String vehicleNumber;

    @Column(name = "from_date")
    private String fromDate;

    @Column(name = "to_date")
    private String toDate;

    @ManyToOne
    @JoinColumn(name = "mobile", referencedColumnName = "mobile")
    private UserEntity userEntity;

}
