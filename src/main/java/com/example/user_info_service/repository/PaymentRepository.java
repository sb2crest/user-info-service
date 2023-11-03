package com.example.user_info_service.repository;

import com.example.user_info_service.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

  @Query("SELECT p FROM PaymentEntity p WHERE p.razorPayOrderId = :razorPayOrderId")
  PaymentEntity findBookingIdByRazorPayOrderId(@Param("razorPayOrderId") String razorPayOrderId);

  @Query("SELECT p FROM PaymentEntity p WHERE p.bookingId = :bookingId")
  PaymentEntity findByBookingId( String bookingId);

}
