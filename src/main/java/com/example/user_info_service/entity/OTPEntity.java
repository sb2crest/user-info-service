package com.example.user_info_service.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


@Setter
@Getter
@Entity
@Table(name = "otp")
public class OTPEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2405172041950251808L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "otp")
    private String otpPassword;

    @Column(name = "generated_time")
    private LocalDateTime generatedTime;

}
