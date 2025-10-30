package com.cognizant.userservice.controller;

import com.cognizant.userservice.model.AttendanceAdjustment;
import com.cognizant.userservice.model.AttendanceAdjustment.AdjustmentStatus;
import com.cognizant.userservice.service.AttendanceAdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing attendance adjustment requests.
 * Provides endpoints for requesting, viewing, approving, and rejecting attendance adjustments.
 */
@RestController
@RequestMapping("/api/v1/attendance/adjustments")
@Slf4j
public class AttendanceAdjustmentController {

    @Autowired
    private AttendanceAdjustmentService attendanceAdjustmentService;

    /**
     * Handles the request to submit a new attendance adjustment.
     *
     * @param principal The authenticated user making the request.
     * @param requestBody A map containing the requested check-in time, check-out time, and reason.
     * Expected keys: "requestedCheckIn", "requestedCheckOut", "reason".
     * @return A ResponseEntity containing the created AttendanceAdjustment object if successful,
     * or a bad request status if required fields are missing.
     */
    @PostMapping("/request")
    public ResponseEntity<AttendanceAdjustment> requestAdjustment(
            Principal principal,
            @RequestBody Map<String, String> requestBody) {
        log.info("Received attendance adjustment request from user: {}", principal.getName());
        // Validate if all required fields are present in the request body
        if (!requestBody.containsKey("requestedCheckIn") || !requestBody.containsKey("requestedCheckOut") || !requestBody.containsKey("reason")) {
            log.warn("Bad request for attendance adjustment: missing required fields.");
            return ResponseEntity.badRequest().build();
        }

        // Parse the LocalDateTime objects from the string representations
        LocalDateTime requestedCheckIn = LocalDateTime.parse(requestBody.get("requestedCheckIn"));
        LocalDateTime requestedCheckOut = LocalDateTime.parse(requestBody.get("requestedCheckOut"));
        String reason = requestBody.get("reason");

        // Call the service to handle the attendance adjustment request
        AttendanceAdjustment adjustment = attendanceAdjustmentService.requestAttendanceAdjustment(principal, requestedCheckIn, requestedCheckOut, reason);
        log.info("Attendance adjustment request created with ID: {}", adjustment.getId());
        return ResponseEntity.ok(adjustment);
    }

    /**
     * Retrieves a list of all pending attendance adjustments.
     * This endpoint is typically for administrators or managers to review requests.
     *
     * @return A ResponseEntity containing a list of pending AttendanceAdjustment objects.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<AttendanceAdjustment>> getPendingAdjustments() {
        log.info("Received request to get all pending attendance adjustments.");
        // Call the service to retrieve pending adjustments
        List<AttendanceAdjustment> pendingAdjustments = attendanceAdjustmentService.getPendingAdjustments();
        return ResponseEntity.ok(pendingAdjustments);
    }

    /**
     * Approves an attendance adjustment request.
     *
     * @param principal The authenticated user approving the request (e.g., admin).
     * @param adjustmentId The unique identifier of the attendance adjustment to approve.
     * @return A ResponseEntity containing the approved AttendanceAdjustment object.
     */
    @PutMapping("/{adjustmentId}/approve")
    public ResponseEntity<AttendanceAdjustment> approveAdjustment(
            Principal principal,
            @PathVariable Long adjustmentId) {
        log.info("Received request to approve attendance adjustment with ID: {} from user: {}", adjustmentId, principal.getName());
        // Call the service to process the approval of the attendance adjustment
        AttendanceAdjustment approvedAdjustment = attendanceAdjustmentService.processAttendanceAdjustment(principal, adjustmentId, AdjustmentStatus.APPROVED);
        return ResponseEntity.ok(approvedAdjustment);
    }

    /**
     * Rejects an attendance adjustment request.
     *
     * @param principal The authenticated user rejecting the request (e.g., manager).
     * @param adjustmentId The unique identifier of the attendance adjustment to reject.
     * @return A ResponseEntity containing the rejected AttendanceAdjustment object.
     */
    @PutMapping("/{adjustmentId}/reject")
    public ResponseEntity<AttendanceAdjustment> rejectAdjustment(
            Principal principal,
            @PathVariable Long adjustmentId) {
        log.info("Received request to reject attendance adjustment with ID: {} from user: {}", adjustmentId, principal.getName());
        // Call the service to process the rejection of the attendance adjustment
        AttendanceAdjustment rejectedAdjustment = attendanceAdjustmentService.processAttendanceAdjustment(principal, adjustmentId, AdjustmentStatus.REJECTED);
        return ResponseEntity.ok(rejectedAdjustment);
    }
}