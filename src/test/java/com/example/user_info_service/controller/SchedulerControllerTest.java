package com.example.user_info_service.controller;

import com.example.user_info_service.scheduler.*;
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

    @MockBean
    TomorrowsBooking tomorrowsBooking;

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
    void getDailyReport() throws Exception {
        Mockito.doNothing().when(dailyReportScheduler).sendDailyReportEmail();

        mvc.perform(get("/dailyReport"))
                .andExpect(status().isOk());
    }

    @Test
    void getWeeklyReport() throws Exception {
        Mockito.doNothing().when(weeklyReportScheduler).sendWeeklyReportEmail();

        mvc.perform(get("/weeklyReport"))
                .andExpect(status().isOk());
    }

    @Test
    void getMonthlyReport() throws Exception {
        Mockito.doNothing().when(monthlyReportScheduler).sendMonthlyReportEmail();

        mvc.perform(get("/monthlyReport"))
                .andExpect(status().isOk());
    }

    @Test
    void getTomorrowsReport() throws Exception {
        Mockito.doNothing().when(tomorrowsBooking).tomorrowsBookingDetails();

        mvc.perform(get("/tomorrowsBooking"))
                .andExpect(status().isOk());
    }

}
