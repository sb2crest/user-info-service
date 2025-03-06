package com.user.info.service.repository;

import com.user.info.service.entity.DestinationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinationRepository extends JpaRepository<DestinationEntity,Long> {

    @Query("select d from DestinationEntity d where d.distance =:distance and d.vehicleNumber IN (:vehicleNumbers)")
    List<DestinationEntity> getAmountData(Double distance, List<String> vehicleNumbers);
}
