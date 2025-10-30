package com.cognizant.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a leave request made by a user.
 */
@Entity
@Table(name = "leave_tbl")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Leave {

    /**
     * The unique identifier for the leave request.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who submitted the leave request.
     * EAGERly fetched.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The start date of the leave period. Cannot be null.
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * The end date of the leave period. Cannot be null.
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * The reason provided by the user for the leave request.
     */
    private String reason;

    /**
     * The current status of the leave request (e.g., PENDING, APPROVED, REJECTED).
     * Defaults to PENDING.
     */
    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;

    /**
     * Optional comment provided by an administrator when processing the leave request.
     */
    private String adminComment;

    /**
     * The total number of days for the leave request. Cannot be null.
     */
    @Column(nullable = false)
    private int numberOfDays;

    /**
     * Defines the possible statuses for a leave request.
     */
    public enum LeaveStatus {
        /** The leave request is awaiting review. */
        PENDING,
        /** The leave request has been approved. */
        APPROVED,
        /** The leave request has been rejected. */
        REJECTED
    }
}