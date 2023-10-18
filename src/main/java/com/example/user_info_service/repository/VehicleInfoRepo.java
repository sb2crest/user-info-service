package com.example.user_info_service.repository;

import com.example.user_info_service.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleInfoRepo extends JpaRepository<VehicleEntity, Integer> {
    @Query("select v from VehicleEntity v where v.vehicleNumber = :vehicleNumber")
    VehicleEntity getByVehicleNumber(String vehicleNumber);

    @Query("select v from VehicleEntity v where" +
            "(COALESCE(:isAC) is null or v.isVehicleAC = :isAC)" +
            "and (COALESCE(:isSleeper) is null or v.isVehicleSleeper = :isSleeper)"+
            " and (COALESCE(:unavailableVehicleList) is null or v.vehicleNumber not in (:unavailableVehicleList))")
    List<VehicleEntity> getAvailableVehicle(List<String> unavailableVehicleList, Boolean isAC, Boolean isSleeper);
}
