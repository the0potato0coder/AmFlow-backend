package com.cognizant.userservice.repository;

import com.cognizant.userservice.model.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

    /**
     * Finds all leave records for a given user ID.
     * @param userId The ID of the user.
     * @return A list of Leave records for the specified user.
     */
    List<Leave> findByUserId(Long userId);

    /**
     * Finds all leave records for a given user ID within a specified start date range.
     * @param userId The ID of the user.
     * @param startDate The start of the date range (inclusive).
     * @param endDate The end of the date range (inclusive).
     * @return A list of Leave records for the user within the date range.
     */
    List<Leave> findByUserIdAndStartDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Finds all leave records with a specific status.
     * @param status The status of the leave requests to find.
     * @return A list of Leave records with the specified status.
     */
    List<Leave> findByStatus(Leave.LeaveStatus status);

    void deleteByUserId(Long userId);
}