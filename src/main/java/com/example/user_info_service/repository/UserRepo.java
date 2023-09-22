package com.example.user_info_service.repository;

import com.example.user_info_service.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<UserEntity , Long> {

    @Query("select u from UserEntity u where u.mobile = :mobile")
    UserEntity getUserByMobileNumber(String mobile);
}
