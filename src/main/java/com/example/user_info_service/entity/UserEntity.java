package com.example.user_info_service.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "user_info")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "email")
    private String email;

  /*  @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<BookingEntity> bookingEntityList;*/

}

