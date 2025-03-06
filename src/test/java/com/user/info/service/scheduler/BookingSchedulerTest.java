package com.user.info.service.scheduler;

import com.user.info.service.entity.SlotsEntity;
import com.user.info.service.repository.SlotsRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class BookingSchedulerTest {

    @InjectMocks
    private BookingScheduler bookingScheduler;

    @Mock
    private SlotsRepo slotsRepo;

    @Test
    void testBookingScheduler(){
        when(slotsRepo.slotToDelete(any(),any())).thenReturn(getSlotEntities());
        doNothing().when(slotsRepo).deleteAll(getSlotEntities());
        bookingScheduler.bookingScheduler();
        verify(slotsRepo,times(1)).slotToDelete(any(),any());
    }

    List<SlotsEntity> getSlotEntities(){
        List<SlotsEntity> slotsEntities = new ArrayList<>();
        SlotsEntity slotsEntity = new SlotsEntity();
        slotsEntity.setFromDate(LocalDate.now());
        slotsEntity.setToDate(LocalDate.now().plusDays(1));
        slotsEntity.setBookingId("NB12vsq");
        slotsEntity.setVehicleNumber("KA01HJ1234");
        slotsEntity.setIsAvailable(false);
        slotsEntities.add(slotsEntity);
        return slotsEntities;
    }

}