package com.example.user_info_service.repository;

import com.example.user_info_service.entity.SlotsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.text.ParseException;

@Repository
public interface SlotsRepo extends JpaRepository<SlotsEntity, Long> {

    @Query("SELECT s.isAvailable FROM SlotsEntity s WHERE s.fromDate <= :toDate AND s.toDate >= :fromDate")
    Boolean findVehicleAvailabilityOnRequiredDate(String fromDate, String toDate) throws ParseException;
}
