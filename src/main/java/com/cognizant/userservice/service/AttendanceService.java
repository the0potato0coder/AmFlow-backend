package com.cognizant.userservice.service;

import com.cognizant.userservice.model.Attendance;
import com.cognizant.userservice.model.User;
import com.cognizant.userservice.repository.AttendanceRepository;
import com.cognizant.userservice.repository.UserRepository;
import com.cognizant.userservice.exception.UserNotFoundException;
import com.cognizant.userservice.exception.ActiveAttendanceExistsException;
import com.cognizant.userservice.exception.NoActiveAttendanceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Attendance checkIn(Principal principal) {
        log.info("Processing check-in for user: {}", principal.getName());

        // Find the user; throws UserNotFoundException if not found.
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.warn("Check-in failed for user {}: User not found.", principal.getName());
                    return new UserNotFoundException("User not found: " + principal.getName());
                });

        // Check for an existing active check-in; throws ActiveAttendanceExistsException if found.
        attendanceRepository.findByUserAndCheckOutTimeIsNull(user).ifPresent(attendance -> {
            log.warn("Check-in failed for user {}: User already has an active check-in.", principal.getName());
            throw new ActiveAttendanceExistsException("Check-in failed: You are already checked in for today.");
        });

        // Create and save new attendance record.
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setCheckInTime(LocalDateTime.now());
        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("User {} checked in successfully. Attendance ID: {}", principal.getName(), savedAttendance.getId());
        return savedAttendance;
    }

    @Transactional
    public Attendance checkOut(Principal principal) {
        log.info("Processing check-out for user: {}", principal.getName());

        // Find the user; throws UserNotFoundException if not found.
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.warn("Check-out failed for user {}: User not found.", principal.getName());
                    return new UserNotFoundException("User not found: " + principal.getName());
                });

        // Find the active check-in; throws NoActiveAttendanceException if none found.
        Attendance attendance = attendanceRepository.findByUserAndCheckOutTimeIsNull(user)
                .orElseThrow(() -> {
                    log.warn("Check-out failed for user {}: No active check-in found for today.", principal.getName());
                    return new NoActiveAttendanceException("Check-out failed: No active check-in found for today.");
                });

        // Update attendance record with check-out time and duration.
        attendance.setCheckOutTime(LocalDateTime.now());
        Duration duration = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime());
        attendance.setTotalDuration(duration.getSeconds());
        attendance.setTotalDurationFormatted(formatDuration(duration));
        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("User {} checked out successfully. Attendance ID: {}", principal.getName(), savedAttendance.getId());
        return savedAttendance;
    }

    /**
     * Retrieves all attendance records for a specified user.
     *
     * @param userId The ID of the user.
     * @return A list of attendance records for the user.
     * @throws UserNotFoundException If the user with the given ID is not found.
     */
    public List<Attendance> getAttendanceDataForUser(Long userId) {
        log.info("Fetching all attendance data for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });

        List<Attendance> allAttendances = attendanceRepository.findByUserOrderByCheckInTimeDesc(user);
        allAttendances.forEach(this::formatAttendanceDuration);

        log.info("Found {} attendance records for user ID: {}", allAttendances.size(), userId);
        return allAttendances;
    }

    /**
     * Retrieves all attendance records for the currently logged-in user.
     *
     * @param principal The security principal representing the logged-in user.
     * @return A list of attendance records for the logged-in user.
     * @throws UserNotFoundException If the logged-in user is not found.
     */
    public List<Attendance> getAllAttendancesForLoggedInUser(Principal principal) {
        log.info("Fetching all attendance data for logged-in user: {}", principal.getName());
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", principal.getName());
                    return new UserNotFoundException("User not found: " + principal.getName());
                });
        List<Attendance> attendances = attendanceRepository.findByUserOrderByCheckInTimeDesc(user);

        attendances.forEach(this::formatAttendanceDuration);
        log.info("Found {} attendance records for user: {}", attendances.size(), principal.getName());
        return attendances;
    }

    /**
     * Retrieves weekly attendance statistics for the currently logged-in user.
     *
     * @param principal The security principal representing the logged-in user.
     * @param year The year for which statistics are requested.
     * @param weekOfYear The week number within the year.
     * @return A map containing weekly attendance statistics.
     * @throws UserNotFoundException If the logged-in user is not found.
     */
    public Map<String, Object> getMyWeeklyStats(Principal principal, int year, int weekOfYear) {
        log.info("Fetching weekly stats for logged-in user: {}, Year: {}, Week: {}", principal.getName(), year, weekOfYear);
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", principal.getName());
                    return new UserNotFoundException("User not found: " + principal.getName());
                });
        return calculateWeeklyStats(user, year, weekOfYear);
    }

    /**
     * Retrieves monthly attendance statistics for the currently logged-in user.
     *
     * @param principal The security principal representing the logged-in user.
     * @param year The year for which statistics are requested.
     * @param month The month number (1-12).
     * @return A map containing monthly attendance statistics.
     * @throws UserNotFoundException If the logged-in user is not found.
     */
    public Map<String, Object> getMyMonthlyStats(Principal principal, int year, int month) {
        log.info("Fetching monthly stats for logged-in user: {}, Year: {}, Month: {}", principal.getName(), year, month);
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", principal.getName());
                    return new UserNotFoundException("User not found: " + principal.getName());
                });
        return calculateMonthlyStats(user, year, month);
    }

    /**
     * Retrieves weekly attendance statistics for a specific user by ID.
     *
     * @param userId The ID of the user.
     * @param year The year for which statistics are requested.
     * @param weekOfYear The week number within the year.
     * @return A map containing weekly attendance statistics.
     * @throws UserNotFoundException If the user with the given ID is not found.
     */
    public Map<String, Object> getWeeklyStats(Long userId, int year, int weekOfYear) {
        log.info("Fetching weekly stats for user ID: {}, Year: {}, Week: {}", userId, year, weekOfYear);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
        return calculateWeeklyStats(user, year, weekOfYear);
    }

    /**
     * Retrieves monthly attendance statistics for a specific user by ID.
     *
     * @param userId The ID of the user.
     * @param year The year for which statistics are requested.
     * @param month The month number (1-12).
     * @return A map containing monthly attendance statistics.
     * @throws UserNotFoundException If the user with the given ID is not found.
     */
    public Map<String, Object> getMonthlyStats(Long userId, int year, int month) {
        log.info("Fetching monthly stats for user ID: {}, Year: {}, Month: {}", userId, year, month);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
        return calculateMonthlyStats(user, year, month);
    }

    /**
     * Formats the total duration of an attendance record into a human-readable string.
     *
     * @param attendance The attendance record to format.
     */
    private void formatAttendanceDuration(Attendance attendance) {
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            Duration duration = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime());
            attendance.setTotalDurationFormatted(formatDuration(duration));
        } else if (attendance.getTotalDuration() != null) {
            attendance.setTotalDurationFormatted(formatDuration(Duration.ofSeconds(attendance.getTotalDuration())));
        } else {
            attendance.setTotalDurationFormatted("N/A");
        }
    }

    /**
     * Calculates weekly attendance statistics for a given user.
     *
     * @param user The user for whom to calculate stats.
     * @param year The year.
     * @param weekOfYear The week number.
     * @return A map containing total hours, total working days, and a daily breakdown.
     */
    private Map<String, Object> calculateWeeklyStats(User user, int year, int weekOfYear) {
        log.debug("Calculating weekly stats for user: {}, Year: {}, Week: {}", user.getUsername(), year, weekOfYear);
        List<Attendance> attendancesInWeek = attendanceRepository.findByUserAndCheckInTimeBetween(
                user,
                getStartOfWeek(year, weekOfYear),
                getEndOfWeek(year, weekOfYear)
        );

        long totalSeconds = attendancesInWeek.stream()
                .filter(a -> a.getTotalDuration() != null)
                .mapToLong(Attendance::getTotalDuration)
                .sum();

        Map<String, Long> dailyBreakdownSeconds = attendancesInWeek.stream()
                .filter(a -> a.getTotalDuration() != null)
                .collect(Collectors.groupingBy(
                        attendance -> attendance.getCheckInTime().toLocalDate().toString(),
                        Collectors.summingLong(Attendance::getTotalDuration)
                ));

        List<Map<String, String>> dailyBreakdownFormatted = dailyBreakdownSeconds.entrySet().stream()
                .map(entry -> {
                    Map<String, String> dailyStat = new LinkedHashMap<>();
                    dailyStat.put("date", entry.getKey());
                    dailyStat.put("totalHours", formatDuration(Duration.ofSeconds(entry.getValue())));
                    return dailyStat;
                })
                .sorted(Comparator.comparing(a -> a.get("date")))
                .collect(Collectors.toList());

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalHoursThisWeek", formatDuration(Duration.ofSeconds(totalSeconds)));
        stats.put("totalWorkingDaysThisWeek", dailyBreakdownSeconds.size());
        stats.put("dailyBreakdown", dailyBreakdownFormatted);
        log.debug("Calculated weekly stats for user {}: {}", user.getUsername(), stats);
        return stats;
    }

    /**
     * Calculates monthly attendance statistics for a given user.
     *
     * @param user The user for whom to calculate stats.
     * @param year The year.
     * @param month The month.
     * @return A map containing total hours and a weekly breakdown.
     */
    private Map<String, Object> calculateMonthlyStats(User user, int year, int month) {
        log.debug("Calculating monthly stats for user: {}, Year: {}, Month: {}", user.getUsername(), year, month);
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        List<Attendance> attendancesInMonth = attendanceRepository.findByUserAndCheckInTimeBetween(
                user,
                startOfMonth,
                endOfMonth
        );

        long totalSeconds = attendancesInMonth.stream()
                .filter(a -> a.getTotalDuration() != null)
                .mapToLong(Attendance::getTotalDuration)
                .sum();

        Map<Integer, Long> weeklyBreakdownSeconds = attendancesInMonth.stream()
                .filter(a -> a.getTotalDuration() != null)
                .collect(Collectors.groupingBy(
                        attendance -> attendance.getCheckInTime().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()),
                        Collectors.summingLong(Attendance::getTotalDuration)
                ));

        Map<String, String> weeklyBreakdownFormatted = weeklyBreakdownSeconds.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        entry -> "Week " + entry.getKey(),
                        entry -> formatDuration(Duration.ofSeconds(entry.getValue())),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalHoursThisMonth", formatDuration(Duration.ofSeconds(totalSeconds)));
        stats.put("weeklyBreakdown", weeklyBreakdownFormatted);
        log.debug("Calculated monthly stats for user {}: {}", user.getUsername(), stats);
        return stats;
    }

    /**
     * Formats a {@link Duration} object into a human-readable string.
     *
     * @param duration The duration to format.
     * @return A string representation of the duration (e.g., "X hours, Y minutes, Z seconds").
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
    }

    /**
     * Calculates the start of a specific week for a given year and week number (ISO standard).
     *
     * @param year The year.
     * @param weekOfYear The week number (1-indexed).
     * @return The {@link LocalDateTime} representing the start of the week.
     */
    private LocalDateTime getStartOfWeek(int year, int weekOfYear) {
        // Get a date in the first week of the year according to ISO standards
        // This is typically Jan 4th, or the Monday of the week containing Jan 4th
        LocalDate firstDayOfFirstISOWeek = LocalDate.of(year, 1, 4)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Now, add (weekOfYear - 1) weeks to get to the start of the desired week.
        // We subtract 1 because weekOfYear is 1-indexed.
        LocalDate startOfTargetWeek = firstDayOfFirstISOWeek.plusWeeks(weekOfYear - 1);

        return startOfTargetWeek.atStartOfDay();
    }

    /**
     * Calculates the end of a specific week for a given year and week number (ISO standard).
     *
     * @param year The year.
     * @param weekOfYear The week number (1-indexed).
     * @return The {@link LocalDateTime} representing the end of the week (last nanosecond of Sunday).
     */
    private LocalDateTime getEndOfWeek(int year, int weekOfYear) {
        LocalDateTime startOfWeek = getStartOfWeek(year, weekOfYear);
        // The end of the week is 7 days after the start, minus one nanosecond to be the very end of the last day.
        return startOfWeek.plusDays(7).minusNanos(1);
    }
}