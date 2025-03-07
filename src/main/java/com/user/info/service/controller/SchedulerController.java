package com.user.info.service.controller;

import com.example.user_info_service.scheduler.*;
import com.user.info.service.scheduler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@CrossOrigin(origins = {"http://localhost:8100","http://nandubus.in"})
public class SchedulerController {

    @Autowired
    DailyReportScheduler dailyReportScheduler;

    @Autowired
    WeeklyReportScheduler weeklyReportScheduler;

    @Autowired
    MonthlyReportScheduler monthlyReportScheduler;

    @Autowired
    TomorrowsBooking tomorrowsBooking;

    @Autowired
    BookingScheduler bookingScheduler;

    @GetMapping("/dailyReport")
    public ResponseEntity<String> getDailyReport() throws Exception {
        dailyReportScheduler.sendDailyReportEmail();
        return ResponseEntity.ok("Reports sent successfully.");
    }

    @GetMapping("/weeklyReport")
    public ResponseEntity<String> getWeeklyReport() throws Exception {
        weeklyReportScheduler.sendWeeklyReportEmail();
        return ResponseEntity.ok("Reports sent successfully.");
    }

    @GetMapping("/monthlyReport")
    public ResponseEntity<String> getMonthlyReport() throws Exception {
        monthlyReportScheduler.sendMonthlyReportEmail();
        return ResponseEntity.ok("Reports sent successfully.");
    }

    @GetMapping("/tomorrowsBooking")
    public ResponseEntity<String> getTomorrowsBookings() throws Exception {
        tomorrowsBooking.tomorrowsBookingDetails();
        return ResponseEntity.ok("Reports sent successfully.");
    }

    @GetMapping("/slots")
    public ResponseEntity<String> bookingScheduler() throws Exception {
        bookingScheduler.bookingScheduler();
        return ResponseEntity.ok("slot deleted successfully.");
    }
}
