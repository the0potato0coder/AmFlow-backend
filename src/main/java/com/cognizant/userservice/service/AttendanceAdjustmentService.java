package com.cognizant.userservice.service;

import com.cognizant.userservice.exception.DataAccessException;
import com.cognizant.userservice.exception.UnauthorizedActionException;
import com.cognizant.userservice.exception.UserNotFoundException;
import com.cognizant.userservice.model.Attendance;
import com.cognizant.userservice.model.AttendanceAdjustment;
import com.cognizant.userservice.model.User;
import com.cognizant.userservice.repository.AttendanceAdjustmentRepository;
import com.cognizant.userservice.repository.AttendanceRepository;
import com.cognizant.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
public class AttendanceAdjustmentService {

    @Autowired
    private AttendanceAdjustmentRepository attendanceAdjustmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    /**
     * Creates a new attendance adjustment request for a user.
     * Accessible by EMPLOYEE, ADMIN for themselves.
     *
     * @param principal The authenticated user requesting the adjustment.
     * @param requestedCheckIn The desired check-in time for the adjustment.
     * @param requestedCheckOut The desired check-out time for the adjustment.
     * @param reason The reason for the adjustment request.
     * @return The created {@link AttendanceAdjustment} request.
     * @throws UserNotFoundException If the requesting user is not found.
     * @throws IllegalArgumentException If the requested check-out time is before the check-in time.
     */
    public AttendanceAdjustment requestAttendanceAdjustment(Principal principal, LocalDateTime requestedCheckIn, LocalDateTime requestedCheckOut, String reason) {
        log.info("User {} is requesting an attendance adjustment from {} to {} for reason: {}", principal.getName(), requestedCheckIn, requestedCheckOut, reason);

        // Find the user; throws UserNotFoundException if not found.
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.warn("Failed to create attendance adjustment: User not found for principal: {}", principal.getName());
                    return new UserNotFoundException("User not found for principal: " + principal.getName());
                });

        // Validate check-in and check-out times.
        if (requestedCheckIn.isAfter(requestedCheckOut)) {
            log.warn("Failed to create attendance adjustment for user {}: Requested check-out {} is before check-in {}.", principal.getName(), requestedCheckOut, requestedCheckIn);
            throw new IllegalArgumentException("Requested check-out time cannot be before requested check-in time.");
        }

        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setUser(user);
        adjustment.setRequestedCheckIn(requestedCheckIn);
        adjustment.setRequestedCheckOut(requestedCheckOut);
        adjustment.setReason(reason);
        adjustment.setStatus(AttendanceAdjustment.AdjustmentStatus.PENDING);

        // Save the adjustment. DataAccessException (or other RuntimeExceptions from persistence)
        // will naturally propagate and trigger transaction rollback.
        AttendanceAdjustment savedAdjustment = attendanceAdjustmentRepository.save(adjustment);
        log.info("Attendance adjustment request created successfully for user {} with ID {}", principal.getName(), savedAdjustment.getId());
        return savedAdjustment;
    }

    /**
     * Retrieves all pending attendance adjustment requests.
     * Accessible only by ADMIN.
     *
     * @return A list of pending {@link AttendanceAdjustment} requests.
     * // @throws DataAccessException If there's an issue retrieving the requests (removed, relying on Spring's default exception handling)
     */
    public List<AttendanceAdjustment> getPendingAdjustments() {
        log.info("Fetching all pending attendance adjustments.");
        // Assuming findByStatus throws unchecked exceptions (e.g., from Spring Data JPA).
        // These exceptions will propagate naturally.
        List<AttendanceAdjustment> adjustments = attendanceAdjustmentRepository.findByStatus(AttendanceAdjustment.AdjustmentStatus.PENDING);
        log.info("Found {} pending adjustments.", adjustments.size());
        return adjustments;
    }

    /**
     * Retrieves a specific attendance adjustment request by ID.
     * Accessible only by ADMIN.
     *
     * @param adjustmentId The ID of the adjustment request.
     * @return The {@link AttendanceAdjustment} request.
     * @throws UserNotFoundException If the adjustment request is not found.
     */
    public AttendanceAdjustment getAdjustmentById(Long adjustmentId) {
        log.info("Fetching attendance adjustment with ID: {}", adjustmentId);
        // Find the adjustment; throws UserNotFoundException if not found.
        return attendanceAdjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> {
                    log.warn("Attendance adjustment request with ID {} not found.", adjustmentId);
                    return new UserNotFoundException("Attendance adjustment request with ID " + adjustmentId + " not found.");
                });
    }

    /**
     * Approves or rejects an attendance adjustment request.
     * Only accessible by ADMIN.
     *
     * @param principal The authenticated ADMIN user processing the request.
     * @param adjustmentId The ID of the adjustment request to approve/reject.
     * @param newStatus The new status ({@link AttendanceAdjustment.AdjustmentStatus#APPROVED APPROVED} or {@link AttendanceAdjustment.AdjustmentStatus#REJECTED REJECTED}).
     * @return The updated {@link AttendanceAdjustment} request.
     * @throws UserNotFoundException If the approving user or adjustment is not found.
     * @throws UnauthorizedActionException If the principal is not an ADMIN.
     * @throws DataAccessException If the attendance adjustment is not in PENDING status.
     */
    public AttendanceAdjustment processAttendanceAdjustment(Principal principal, Long adjustmentId, AttendanceAdjustment.AdjustmentStatus newStatus) {
        log.info("Admin {} is processing attendance adjustment ID {} with status {}", principal.getName(), adjustmentId, newStatus);

        // Find the admin user; throws UserNotFoundException if not found.
        User adminUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.warn("Failed to process attendance adjustment ID {}: Admin user not found for principal: {}", adjustmentId, principal.getName());
                    return new UserNotFoundException("Admin user not found for principal: " + principal.getName());
                });

        // Check if the user has ADMIN role.
        if (!adminUser.getRole().equals(User.Role.ADMIN)) {
            log.warn("Unauthorized attempt to process attendance adjustment ID {} by non-admin user: {}", adjustmentId, principal.getName());
            throw new UnauthorizedActionException("Only ADMIN can process attendance adjustments.");
        }

        // Find the attendance adjustment; throws UserNotFoundException if not found.
        AttendanceAdjustment adjustment = attendanceAdjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> {
                    log.warn("Failed to process attendance adjustment: Request with ID {} not found.", adjustmentId);
                    return new UserNotFoundException("Attendance adjustment request with ID " + adjustmentId + " not found.");
                });

        // Ensure the adjustment is in PENDING status before processing.
        if (adjustment.getStatus() != AttendanceAdjustment.AdjustmentStatus.PENDING) {
            log.warn("Failed to process attendance adjustment ID {}: Current status is {}. Only PENDING requests can be processed.", adjustmentId, adjustment.getStatus());
            throw new DataAccessException("Attendance adjustment request is not in PENDING status and cannot be processed.");
        }

        adjustment.setStatus(newStatus);
        adjustment.setApprovedBy(adminUser);
        adjustment.setActionTakenAt(LocalDateTime.now());

        if (newStatus == AttendanceAdjustment.AdjustmentStatus.APPROVED) {
            log.info("Attendance adjustment ID {} approved. Creating new attendance record.", adjustmentId);
            Attendance newAttendance = new Attendance();
            newAttendance.setUser(adjustment.getUser());
            newAttendance.setCheckInTime(adjustment.getRequestedCheckIn());
            newAttendance.setCheckOutTime(adjustment.getRequestedCheckOut());

            // Calculate duration for the new attendance record.
            if (newAttendance.getCheckInTime() != null && newAttendance.getCheckOutTime() != null) {
                Duration duration = Duration.between(newAttendance.getCheckInTime(), newAttendance.getCheckOutTime());
                newAttendance.setTotalDuration(duration.getSeconds());
            } else {
                newAttendance.setTotalDuration(0L); // Default to 0 if times are not set
            }

            // Save the new attendance record. Any persistence exceptions will propagate.
            attendanceRepository.save(newAttendance);
            log.info("New attendance record created for adjustment ID {}", adjustmentId);

        } else if (newStatus == AttendanceAdjustment.AdjustmentStatus.REJECTED) {
            log.info("Attendance adjustment ID {} was rejected.", adjustmentId);
        }

        // Save the updated adjustment. Any persistence exceptions will propagate.
        AttendanceAdjustment processedAdjustment = attendanceAdjustmentRepository.save(adjustment);
        log.info("Attendance adjustment ID {} processed successfully.", adjustmentId);
        return processedAdjustment;
    }
}