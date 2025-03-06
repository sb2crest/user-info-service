package com.user.info.service.repository;

import com.user.info.service.entity.SlotsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlotsRepo extends JpaRepository<SlotsEntity, Long> {

    @Query("SELECT COUNT(s) > 0 FROM SlotsEntity s WHERE s.vehicleNumber = :vehicleNumber AND s.fromDate <= :toDate AND s.toDate >= :fromDate")
    Boolean findVehicleAvailabilityOnRequiredDate(@Param("vehicleNumber") String vehicleNumber, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate) throws ParseException;

    @Query("SELECT s FROM SlotsEntity s WHERE s.bookingId = :bookingId")
    SlotsEntity findByBookingId(String bookingId);

    @Query("SELECT s FROM SlotsEntity s WHERE s.vehicleNumber = :vehicleNumber")
    List<SlotsEntity> getByVehicleNUmber(String vehicleNumber);

//    @Query("SELECT s FROM SlotsEntity s " +
//            "WHERE s.bookingId IN (select b.bookingId from BookingEntity b " +
//            "where b.bookingDate >= :fiveMin and b.bookingDate <= sixMin and b.bookingStatus = 'E')")
//    List<SlotsEntity> slotToDelete(LocalDateTime fiveMin, LocalDateTime sixMin);

    @Query("SELECT s FROM SlotsEntity s " +
            "WHERE s.bookingId IN (SELECT b.bookingId FROM BookingEntity b " +
            "WHERE (b.bookingDate BETWEEN :sixMin AND :fiveMin) AND b.bookingStatus = 'E')")
    List<SlotsEntity> slotToDelete(@Param("fiveMin") LocalDateTime fiveMin, @Param("sixMin") LocalDateTime sixMin);
}
