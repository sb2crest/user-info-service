package com.example.user_info_service.repository;

import com.example.user_info_service.entity.MasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterEntityRepo extends JpaRepository<MasterEntity,Long> {


    @Query("SELECT m FROM MasterEntity m WHERE LOWER(m.source) = LOWER(:source) AND LOWER(m.destination) = LOWER(:destination) AND m.vehicleNumber = :vehicleNumber")
    MasterEntity findBySourceAndDestination(String source, String destination, String vehicleNumber);

    @Query("select m from MasterEntity m where m.source = :trip and m.vehicleNumber = :vehicleNumber")
    MasterEntity findTripAmount(String trip, String vehicleNumber);
}
