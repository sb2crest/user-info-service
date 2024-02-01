package com.example.user_info_service.repository;

import com.example.user_info_service.entity.PaymentEntity;
import com.example.user_info_service.entity.SlotsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

  @Query("SELECT p FROM PaymentEntity p WHERE p.razorPayOrderId = :razorPayOrderId")
  PaymentEntity findBookingIdByRazorPayOrderId(@Param("razorPayOrderId") String razorPayOrderId);

  @Query("SELECT p FROM PaymentEntity p WHERE p.bookingId = :bookingId")
  List<PaymentEntity> findByBookingId(String bookingId);

  @Query("SELECT p FROM PaymentEntity p WHERE p.bookingId = " +
          "(SELECT s.bookingId FROM SlotsEntity s WHERE s.vehicleNumber = :vehicleNumber AND " +
          "(s.fromDate NOT BETWEEN :fromDate AND :toDate) AND " +
          "(s.toDate NOT BETWEEN :fromDate AND :toDate))")
  PaymentEntity isSlotBooked(LocalDate fromDate, LocalDate toDate, String vehicleNumber);
}
