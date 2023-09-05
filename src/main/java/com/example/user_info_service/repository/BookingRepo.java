package com.example.user_info_service.repository;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.pojo.BookingPojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.List;

@Repository
public interface BookingRepo extends JpaRepository<BookingEntity, Long> {

}
