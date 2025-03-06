package com.user.info.service.repository;

import com.user.info.service.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<UserEntity , Long> {

    @Query("select u from UserEntity u where u.mobile = :mobile")
    UserEntity getUserByMobileNumber(String mobile);
}
