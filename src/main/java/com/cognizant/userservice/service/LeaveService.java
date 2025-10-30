package com.cognizant.userservice.service;

import com.cognizant.userservice.exception.InvalidLeaveRequestException;
import com.cognizant.userservice.model.Leave;
import com.cognizant.userservice.model.User;
import com.cognizant.userservice.repository.LeaveRepository;
import com.cognizant.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private UserRepository userRepository;

    private static final int MONTHLY_LEAVE_QUOTA = 3;

    public Leave applyLeave(Leave leave, String username) {
        log.info("Applying leave for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new IllegalArgumentException("User not found");
                });

        validateLeaveRequest(leave, user);

        leave.setUser(user);
        leave.setStatus(Leave.LeaveStatus.PENDING);
        leave.setNumberOfDays(calculateLeaveDays(leave.getStartDate(), leave.getEndDate()));

        Leave savedLeave = leaveRepository.save(leave);
        log.info("Leave applied successfully for user: {}. Leave ID: {}", username, savedLeave.getId());
        return savedLeave;
    }

    public Leave processLeaveRequest(Long leaveId, Leave.LeaveStatus status, String adminComment) {
        log.info("Processing leave request for leave ID: {}. Status: {}, Admin Comment: {}", leaveId, status, adminComment);
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> {
                    log.warn("Leave request not found for ID: {}", leaveId);
                    return new IllegalArgumentException("Leave request not found");
                });

        leave.setStatus(status);
        leave.setAdminComment(adminComment);

        Leave updatedLeave = leaveRepository.save(leave);
        log.info("Leave request processed successfully for leave ID: {}", leaveId);
        return updatedLeave;
    }

    private void validateLeaveRequest(Leave leave, User user) {
        log.debug("Validating leave request for user: {}", user.getUsername());
        if (leave.getStartDate() == null) {
            log.warn("Leave validation failed: Start date is null.");
            throw new InvalidLeaveRequestException("Start date cannot be null");
        }
        if (leave.getEndDate() == null) {
            log.warn("Leave validation failed: End date is null.");
            throw new InvalidLeaveRequestException("End date cannot be null");
        }

        if (leave.getStartDate().isBefore(LocalDate.now())) {
            log.warn("Leave validation failed: Leave applied for past dates.");
            throw new InvalidLeaveRequestException("Leave cannot be applied for past dates");
        }

        if (leave.getEndDate().isBefore(leave.getStartDate())) {
            log.warn("Leave validation failed: End date is before start date.");
            throw new InvalidLeaveRequestException("End date cannot be before start date");
        }

        int leaveDays = calculateLeaveDays(leave.getStartDate(), leave.getEndDate());

        // Check monthly quota
        LocalDate monthStart = leave.getStartDate().withDayOfMonth(1);
        LocalDate monthEnd = leave.getStartDate().plusMonths(1).withDayOfMonth(1).minusDays(1);

        List<Leave> monthlyLeaves = leaveRepository.findByUserIdAndStartDateBetween(
                user.getId(), monthStart, monthEnd);

        int usedLeaveDays = monthlyLeaves.stream()
                .filter(l -> l.getStatus() != Leave.LeaveStatus.REJECTED)
                .mapToInt(Leave::getNumberOfDays)
                .sum();

        if (usedLeaveDays + leaveDays > MONTHLY_LEAVE_QUOTA) {
            log.warn("Leave validation failed: Monthly leave quota exceeded for user: {}", user.getUsername());
            throw new InvalidLeaveRequestException(
                    String.format("Monthly leave quota exceeded. Available: %d, Requested: %d",
                            MONTHLY_LEAVE_QUOTA - usedLeaveDays, leaveDays));
        }
        log.debug("Leave request validation successful for user: {}", user.getUsername());
    }

    private int calculateLeaveDays(LocalDate startDate, LocalDate endDate) {
        int days = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        log.debug("Calculated leave days: {}. From {} to {}", days, startDate, endDate);
        return days;
    }

    public List<Leave> getLeavesByUser(String username) {
        log.info("Fetching leaves for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new IllegalArgumentException("User not found");
                });
        return leaveRepository.findByUserId(user.getId());
    }

    public List<Leave> getPendingLeaves() {
        log.info("Fetching all pending leaves.");
        return leaveRepository.findByStatus(Leave.LeaveStatus.PENDING);
    }
}