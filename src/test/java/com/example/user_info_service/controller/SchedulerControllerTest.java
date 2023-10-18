package com.example.user_info_service.controller;

import com.example.user_info_service.scheduler.DailyReportScheduler;
import com.example.user_info_service.scheduler.MonthlyReportScheduler;
import com.example.user_info_service.scheduler.WeeklyReportScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SchedulerController.class)
class SchedulerControllerTest {

    @MockBean
    DailyReportScheduler dailyReportScheduler;

    @MockBean
    WeeklyReportScheduler weeklyReportScheduler;

    @MockBean
    MonthlyReportScheduler monthlyReportScheduler;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void getReport() throws Exception {
        Mockito.doNothing().when(dailyReportScheduler).sendDailyReportEmail();
        Mockito.doNothing().when(weeklyReportScheduler).sendWeeklyReportEmail();
        Mockito.doNothing().when(monthlyReportScheduler).sendMonthlyReportEmail();

        mvc.perform(get("/report"))
                .andExpect(status().isOk());
    }
}
