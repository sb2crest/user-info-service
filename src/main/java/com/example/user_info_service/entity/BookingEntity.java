package com.example.user_info_service.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "booking_info")
public class BookingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2405172041950251808L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "booking_status")
    private String bookingStatus;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @ManyToOne
    @JoinColumn(name = "mobile",referencedColumnName = "mobile", updatable = false, insertable = false)
    private UserEntity userEntity;

    @OneToMany(mappedBy = "bookingEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentEntity> paymentEntities;

}
