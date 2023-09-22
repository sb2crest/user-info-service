package com.example.user_info_service.repository;

import com.example.user_info_service.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleInfoRepo extends JpaRepository<VehicleEntity,Integer> {
    @Query("select v from VehicleEntity v where v.vehicleNumber = :vehicleNumber")
    VehicleEntity getByVehicleNumber(String vehicleNumber);
}
