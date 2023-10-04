package com.example.user_info_service.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SchedulerController {

    @Autowired
    DailyReportScheduler schedulerController;

    @Autowired
    WeeklyReportScheduler weeklyReportScheduler;

    @GetMapping("/report")
    void getReport(){
         schedulerController.sendDailyReportEmail();
         //weeklyReportScheduler.sendWeeklyReportEmail();
    }
}
