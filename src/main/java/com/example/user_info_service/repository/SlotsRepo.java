package com.example.user_info_service.repository;

import com.example.user_info_service.entity.SlotsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlotsRepo extends JpaRepository<SlotsEntity, Long> {

    @Query("SELECT COUNT(s) > 0 FROM SlotsEntity s WHERE s.vehicleNumber = :vehicleNumber AND s.fromDate <= :toDate AND s.toDate >= :fromDate")
    Boolean findVehicleAvailabilityOnRequiredDate(@Param("vehicleNumber") String vehicleNumber, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate) throws ParseException;

    @Query("SELECT s FROM SlotsEntity s WHERE s.bookingId = :bookingId")
    SlotsEntity findByBookingId(String bookingId);

    @Query("SELECT s FROM SlotsEntity s WHERE s.vehicleNumber = :vehicleNumber")
    List<SlotsEntity> getByVehicleNUmber(String vehicleNumber);

    @Query("SELECT DISTINCT s.vehicleNumber FROM SlotsEntity s " +
            "WHERE s.vehicleNumber IN (" +
            "    SELECT DISTINCT s1.vehicleNumber FROM SlotsEntity s1 " +
            "    WHERE (s1.fromDate BETWEEN :fromDate AND :toDate) or" +
            "    (s1.toDate BETWEEN :fromDate AND :toDate)"+
            ")")
    List<String> getUnavailableList(LocalDate fromDate, LocalDate toDate);
}
