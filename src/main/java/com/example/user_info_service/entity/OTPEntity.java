package com.example.user_info_service.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Setter
@Getter
@Entity
@Table(name = "otp")
public class OTPEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "otp")
    private String otpPassword;

    @Column(name = "generated_time")
    private String generatedTime;

}
