package com.cognizant.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a request to adjust an attendance record.
 * This is typically used for corrections or manual entries.
 */
@Entity
@Table(name = "attendance_adjustment_tbl")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime requestedCheckIn;

    private LocalDateTime requestedCheckOut;

    private String reason;

    @Enumerated(EnumType.STRING)
    private AdjustmentStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    private LocalDateTime actionTakenAt;

    public enum AdjustmentStatus {
        PENDING, APPROVED, REJECTED
    }
}