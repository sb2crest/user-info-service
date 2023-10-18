package com.example.user_info_service.controller;

import com.example.user_info_service.scheduler.DailyReportScheduler;
import com.example.user_info_service.scheduler.MonthlyReportScheduler;
import com.example.user_info_service.scheduler.WeeklyReportScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SchedulerController {

    @Autowired
    DailyReportScheduler dailyReportScheduler;

    @Autowired
    WeeklyReportScheduler weeklyReportScheduler;

    @Autowired
    MonthlyReportScheduler monthlyReportScheduler;

    @GetMapping("/report")
    public ResponseEntity<String> getReport() throws Exception {
        dailyReportScheduler.sendDailyReportEmail();
        weeklyReportScheduler.sendWeeklyReportEmail();
        monthlyReportScheduler.sendMonthlyReportEmail();
        return ResponseEntity.ok("Reports sent successfully.");
    }
}
