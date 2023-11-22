package com.example.user_info_service.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

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

    @ElementCollection
    @Column(name = "file_url")
    private List<String> s3ImageUrl;

    @Column(name = "filter")
    private String filter;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_number")
    private String driverNumber;

    @Column(name = "alternative_number")
    private String alternateNumber;

    @Column(name = "emergency_contact_number")
    private String emergencyNumber;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        VehicleEntity that = (VehicleEntity) o;
        return vId != null && Objects.equals(vId, that.vId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
