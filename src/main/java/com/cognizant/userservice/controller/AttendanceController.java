package com.cognizant.userservice.controller;

import com.cognizant.userservice.model.Attendance;
import com.cognizant.userservice.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@Slf4j
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * Endpoint for an authenticated user to check in.
     *
     * @param principal The security principal representing the currently authenticated user.
     * @return A ResponseEntity containing the created Attendance record.
     */
    @PostMapping("/checkin")
    public ResponseEntity<Attendance> checkIn(Principal principal) {
        log.info("Received request for user {} to check in", principal.getName());
        Attendance attendance = attendanceService.checkIn(principal);
        return new ResponseEntity<>(attendance, HttpStatus.CREATED);
    }

    /**
     * Endpoint for an authenticated user to check out.
     *
     * @param principal The security principal representing the currently authenticated user.
     * @return A ResponseEntity containing the updated Attendance record.
     */
    @PutMapping("/checkout")
    public ResponseEntity<Attendance> checkOut(Principal principal) {
        log.info("Received request for user {} to check out", principal.getName());
        Attendance attendance = attendanceService.checkOut(principal);
        return ResponseEntity.ok(attendance);
    }

    /**
     * Endpoint for an ADMIN to retrieve all attendance records for a specific user.
     *
     * @param userId The unique identifier of the user.
     * @return A ResponseEntity containing a map of attendance data.
     */
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<Attendance>> getAllAttendancesForUser(@PathVariable Long userId) {
        log.info("Admin request to get all attendances for user ID: {}", userId);
        // attendanceService.getAttendanceDataForUser handles UserNotFoundException
        List<Attendance> response = attendanceService.getAttendanceDataForUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * NEW: Endpoint for an authenticated user to retrieve all their own attendance records.
     *
     * @param principal The security principal representing the currently authenticated user.
     * @return A ResponseEntity containing a list of the user's attendance records.
     */
    @GetMapping("/my-all")
    public ResponseEntity<List<Attendance>> getMyAllAttendances(Principal principal) {
        log.info("Request to get all attendances for user: {}", principal.getName());
        if (principal == null) {
            log.warn("Unauthorized request to get all attendances: principal is null.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Attendance> attendances = attendanceService.getAllAttendancesForLoggedInUser(principal);
        return ResponseEntity.ok(attendances);
    }


    /**
     * Endpoint for an ADMIN to retrieve weekly attendance statistics for a specific user.
     *
     * @param userId      the unique identifier of the user.
     * @param year        the year for which stats are requested.
     * @param weekOfYear the week number within the year.
     * @return a {@link ResponseEntity} containing the weekly statistics.
     */
    @GetMapping("/user/{userId}/stats/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyStatsForUser(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int weekOfYear) {
        log.info("Admin request for weekly stats for user ID: {}, Year: {}, Week: {}", userId, year, weekOfYear);
        // attendanceService.getWeeklyStats handles UserNotFoundException
        Map<String, Object> stats = attendanceService.getWeeklyStats(userId, year, weekOfYear);
        return ResponseEntity.ok(stats);
    }

    /**
     * Endpoint for an ADMIN to retrieve monthly attendance statistics for a specific user.
     *
     * @param userId the unique identifier of the user.
     * @param year   the year for which stats are requested.
     * @param month  the month number (1-12).
     * @return a {@link ResponseEntity} containing the monthly statistics.
     */
    @GetMapping("/user/{userId}/stats/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyStatsForUser(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        log.info("Admin request for monthly stats for user ID: {}, Year: {}, Month: {}", userId, year, month);
        // attendanceService.getMonthlyStats handles UserNotFoundException
        Map<String, Object> stats = attendanceService.getMonthlyStats(userId, year, month);
        return ResponseEntity.ok(stats);
    }

    /**
     * Endpoint for an authenticated user to retrieve their own weekly attendance statistics.
     *
     * @param principal  the security principal representing the currently authenticated user.
     * @param year       the year for which stats are requested.
     * @param weekOfYear the week number within the year.
     * @return a {@link ResponseEntity} containing the weekly statistics for the logged-in user.
     */
    @GetMapping("/my-stats/weekly")
    public ResponseEntity<Map<String, Object>> getMyWeeklyStats(
            Principal principal,
            @RequestParam int year,
            @RequestParam int weekOfYear) {
        log.info("Request for weekly stats for user: {}, Year: {}, Week: {}", principal.getName(), year, weekOfYear);
        if (principal == null) {
            log.warn("Unauthorized request for weekly stats: principal is null.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // attendanceService.getMyWeeklyStats handles UserNotFoundException based on principal
        Map<String, Object> stats = attendanceService.getMyWeeklyStats(principal, year, weekOfYear);
        return ResponseEntity.ok(stats);
    }

    /**
     * Endpoint for an authenticated user to retrieve their own monthly attendance statistics.
     *
     * @param principal the security principal representing the currently authenticated user.
     * @param year      the year for which stats are requested.calculateMonthlyStats
     * @param month     the month number (1-12).
     * @return a {@link ResponseEntity} containing the monthly statistics for the logged-in user.
     */
    @GetMapping("/my-stats/monthly")
    public ResponseEntity<Map<String, Object>> getMyMonthlyStats(
            Principal principal,
            @RequestParam int year,
            @RequestParam int month) {
        log.info("Request for monthly stats for user: {}, Year: {}, Month: {}", principal.getName(), year, month);
        if (principal == null) {
            log.warn("Unauthorized request for monthly stats: principal is null.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // attendanceService.getMyMonthlyStats handles UserNotFoundException based on principal
        Map<String, Object> stats = attendanceService.getMyMonthlyStats(principal, year, month);
        return ResponseEntity.ok(stats);
    }
}