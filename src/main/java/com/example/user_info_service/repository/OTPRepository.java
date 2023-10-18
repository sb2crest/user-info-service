package com.example.user_info_service.repository;

import com.example.user_info_service.entity.OTPEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OTPRepository extends JpaRepository<OTPEntity,Long> {
    @Query("SELECT o FROM OTPEntity o WHERE o.mobile = :mobile AND o.generatedTime >= :localDateTime ORDER BY o.generatedTime DESC")
    List<OTPEntity> findByPhoneNumber(@Param("mobile") String mobile, @Param("localDateTime") LocalDateTime localDateTime);

}
