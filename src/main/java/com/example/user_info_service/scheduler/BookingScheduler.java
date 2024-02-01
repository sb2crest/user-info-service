package com.example.user_info_service.scheduler;

import com.example.user_info_service.entity.SlotsEntity;
import com.example.user_info_service.repository.SlotsRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BookingScheduler {

    @Autowired
    private SlotsRepo slotsRepo;


    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Kolkata")
    public void bookingScheduler() {
        LocalDateTime fiveMin = LocalDateTime.now().minusMinutes(5);
        LocalDateTime sixMin = LocalDateTime.now().minusMinutes(6);

        List<SlotsEntity> slotsEntities = slotsRepo.slotToDelete(fiveMin, sixMin);
        slotsRepo.deleteAll(slotsEntities);
    }
}
