package com.cognizant.userservice.controller;

import com.cognizant.userservice.model.Leave;
import com.cognizant.userservice.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing leave requests.
 * Provides endpoints for applying for leave, viewing personal leave history,
 * viewing all pending leave requests, and processing leave requests (approving/rejecting).
 */
@RestController
@RequestMapping("/api/v1/leaves")
@Slf4j
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    /**
     * Handles the request to apply for a new leave.
     *
     * @param leave The Leave object containing details of the leave request (e.g., type, start date, end date).
     * @param principal The authenticated user applying for leave.
     * @return A ResponseEntity containing the applied Leave object.
     */
    @PostMapping("/apply")
    public ResponseEntity<Leave> applyLeave(@RequestBody Leave leave, Principal principal) {
        log.info("Received request to apply leave for user: {}", principal.getName());
        // Call the service to handle the leave application
        Leave appliedLeave = leaveService.applyLeave(leave, principal.getName());
        return ResponseEntity.ok(appliedLeave);
    }

    /**
     * Retrieves a list of all leave requests submitted by the authenticated user.
     *
     * @param principal The authenticated user whose leave history is being requested.
     * @return A ResponseEntity containing a list of Leave objects associated with the user.
     */
    @GetMapping("/my-leaves")
    public ResponseEntity<List<Leave>> getMyLeaves(Principal principal) {
        log.info("Received request to get leaves for user: {}", principal.getName());
        // Call the service to retrieve leaves for the current user
        List<Leave> leaves = leaveService.getLeavesByUser(principal.getName());
        return ResponseEntity.ok(leaves);
    }

    /**
     * Retrieves a list of all pending leave requests.
     * This endpoint is typically for administrators or managers to review leave applications.
     *
     * @return A ResponseEntity containing a list of Leave objects with a pending status.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Leave>> getPendingLeaves() {
        log.info("Received request to get all pending leaves");
        // Call the service to retrieve all pending leave requests
        List<Leave> pendingLeaves = leaveService.getPendingLeaves();
        return ResponseEntity.ok(pendingLeaves);
    }

    /**
     * Processes a leave request, changing its status (e.g., to APPROVED or REJECTED).
     * This endpoint is typically used by administrators or managers.
     *
     * @param leaveId The unique identifier of the leave request to be processed.
     * @param status The new status to be set for the leave request (e.g., APPROVED, REJECTED).
     * @param adminComment An optional comment from the administrator/manager regarding the processing of the leave.
     * @return A ResponseEntity containing the processed Leave object.
     */
    @PutMapping("/{leaveId}")
    public ResponseEntity<Leave> processLeaveRequest(
            @PathVariable Long leaveId,
            @RequestParam Leave.LeaveStatus status,
            @RequestParam(required = false) String adminComment) {
        log.info("Received request to process leave request for leave ID: {}. Status: {}, Admin Comment: {}", leaveId, status, adminComment);
        // Call the service to process the leave request with the given status and comment
        Leave processedLeave = leaveService.processLeaveRequest(leaveId, status, adminComment);
        return ResponseEntity.ok(processedLeave);
    }
}