package com.example.user_info_service.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "slots_info")
public class SlotsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "from_date")
    private String fromDate;

    @Column(name = "to_date")
    private String toDate;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @ManyToOne
    @JoinColumn(name = "vehicle_number", referencedColumnName = "vehicle_number", insertable = false, updatable = false)
    private BookingEntity bookingEntity;
}
