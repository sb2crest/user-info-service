package com.example.user_info_service.repository;

import com.example.user_info_service.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehicleInfoRepo extends JpaRepository<VehicleEntity, Integer> {
    @Query("select v from VehicleEntity v where v.vehicleNumber = :vehicleNumber")
    VehicleEntity getByVehicleNumber(String vehicleNumber);

    @Query("SELECT DISTINCT v FROM VehicleEntity v " +
            "WHERE NOT EXISTS (" +
            "    SELECT s FROM SlotsEntity s " +
            "    WHERE s.vehicleNumber = v.vehicleNumber " +
            "      AND (:fromDate BETWEEN s.fromDate AND s.toDate OR :toDate BETWEEN s.fromDate AND s.toDate)) " +
            "AND (COALESCE(:filter) IS NULL OR v.filter IN :filter)")
    List<VehicleEntity> getAvailableVehicle(@Param("filter") List<String> filter, LocalDate fromDate, LocalDate toDate );

}
