package com.example.user_info_service.repository;

import com.example.user_info_service.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface BookingRepo extends JpaRepository<BookingEntity, Long> {

    @Query("select b from BookingEntity b where b.bookingId = :bookingId")
    BookingEntity getByBookingId(String bookingId);
}
