package com.example.user_info_service.repository;

import com.example.user_info_service.entity.SlotsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.List;

@Repository
public interface SlotsRepo extends JpaRepository<SlotsEntity, Long> {

    @Query("SELECT COUNT(s) > 0 FROM SlotsEntity s WHERE s.vehicleNumber = :vehicleNumber AND s.fromDate <= :toDate AND s.toDate >= :fromDate")
    Boolean findVehicleAvailabilityOnRequiredDate(@Param("vehicleNumber") String vehicleNumber, @Param("fromDate") String fromDate, @Param("toDate") String toDate) throws ParseException;

    @Query("SELECT s FROM SlotsEntity s WHERE s.bookingId = :bookingId")
    SlotsEntity findByBookingId(String bookingId);

    @Query("SELECT s FROM SlotsEntity s WHERE s.vehicleNumber = :vehicleNumber")
    List<SlotsEntity> getByVehicleNUmber(String vehicleNumber);


}
