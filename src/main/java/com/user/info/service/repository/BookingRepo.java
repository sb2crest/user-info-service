package com.user.info.service.repository;

import com.user.info.service.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface BookingRepo extends JpaRepository<BookingEntity, Long> {

    @Query("select b from BookingEntity b where b.bookingId = :bookingId")
    BookingEntity getByBookingId(String bookingId);

    @Query("select b from BookingEntity b where b.fromDate = :tomorrow")
    List<BookingEntity> getTomorrowsBooking(LocalDate tomorrow);

    @Query("select b from BookingEntity b where b.bookingDate = :bookingDate")
    List<BookingEntity> getReport(LocalDate bookingDate);

    @Query("select b from BookingEntity b where b.bookingDate >= :fromDate and b.bookingDate <= :toDate order by b.bookingDate asc")
    List<BookingEntity> getReportForWeeklyAndMonthly(LocalDate fromDate, LocalDate toDate);

//    @Query("select b from BookingEntity b where b.bookingId = :bookingId and b.mobile = :mobile")
    @Query("select b from BookingEntity b where b.bookingId = :bookingId and b.mobile = :mobile")
    BookingEntity validateUsingIdAndMobile(String bookingId, String mobile);

    @Query("select b from BookingEntity b where b.mobile = :mobile")
    List<BookingEntity> getByMobileNumber(String mobile);
}
