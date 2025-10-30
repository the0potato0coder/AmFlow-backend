package com.cognizant.userservice.repository;

import com.cognizant.userservice.model.AttendanceAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceAdjustmentRepository extends JpaRepository<AttendanceAdjustment, Long> {
    List<AttendanceAdjustment> findByStatus(AttendanceAdjustment.AdjustmentStatus status);
    List<AttendanceAdjustment> findByUser_Id(Long userId);
    void deleteByUserId(Long userId);
}