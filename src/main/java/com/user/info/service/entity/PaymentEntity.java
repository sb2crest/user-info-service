package com.user.info.service.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payment_info")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long orderId;

    @Column(name = "razorpay_order_id")
    private String razorPayOrderId;

    @Column(name = "amount")
    private double amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "razorpay_payment_id")
    private String razorPayPaymentId;

    @Column(name = "payment_status")
    private String paymentStatus;

    @ManyToOne
    @JoinColumn(name = "booking_id",referencedColumnName = "booking_id", updatable = false, insertable = false)
    private BookingEntity bookingEntity;
}
