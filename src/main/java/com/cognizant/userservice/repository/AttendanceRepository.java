package com.cognizant.userservice.repository;

import com.cognizant.userservice.model.Attendance;
import com.cognizant.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserAndCheckOutTimeIsNull(User user);

    List<Attendance> findByUserOrderByCheckInTimeDesc(User user);

    List<Attendance> findByUserAndCheckInTimeBetween(User user, LocalDateTime startTime, LocalDateTime endTime);

    void deleteByUserId(Long userId);
}