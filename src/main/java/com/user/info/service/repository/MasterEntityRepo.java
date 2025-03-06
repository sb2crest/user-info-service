package com.user.info.service.repository;

import com.user.info.service.entity.MasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterEntityRepo extends JpaRepository<MasterEntity,Long> {


    @Query("SELECT m FROM MasterEntity m WHERE LOWER(m.source) = LOWER(:source) AND LOWER(m.destination) = LOWER(:destination) AND m.vehicleNumber IN (:vehicleNumbers)")
    List<MasterEntity> findBySourceAndDestination(String source, String destination, List<String> vehicleNumbers);

    @Query("select m from MasterEntity m where m.source = :trip and m.vehicleNumber IN :vehicleNumbers")
    List<MasterEntity> findTripAmount(String trip, List<String> vehicleNumbers);
}
