package com.example.user_info_service.repository;

import com.example.user_info_service.entity.DestinationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationRepository extends JpaRepository<DestinationEntity,Long> {

    @Query("select d from DestinationEntity d where d.distance =:distance")
    DestinationEntity getAmountData(Double distance);
}
