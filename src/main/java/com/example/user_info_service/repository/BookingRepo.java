package com.example.user_info_service.repository;

import com.example.user_info_service.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BookingRepo extends JpaRepository<BookingEntity, Long> {

    @Query("select b from BookingEntity b where b.bookingId = :bookingId")
    BookingEntity getByBookingId(String bookingId);

    @Query("select b from BookingEntity b where b.bookingDate = :bookingDate")
    List<BookingEntity> getTodaysReport(String bookingDate);

    @Query("SELECT b FROM BookingEntity b WHERE b.bookingDate <= :startDate AND b.bookingDate <= :endDate")
    List<BookingEntity> getWeeklyReport(String startDate, String endDate);
}
