package com.cognizant.userservice.service;

import com.cognizant.userservice.exception.UnauthorizedActionException;
import com.cognizant.userservice.exception.UserNotFoundException;
import com.cognizant.userservice.model.AttendanceAdjustment;
import com.cognizant.userservice.model.User;
import com.cognizant.userservice.repository.AttendanceAdjustmentRepository;
import com.cognizant.userservice.repository.AttendanceRepository;
import com.cognizant.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceAdjustmentServiceTest {

    @Mock
    private AttendanceAdjustmentRepository attendanceAdjustmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceAdjustmentService attendanceAdjustmentService;

    private User user;
    private User admin;
    private Principal userPrincipal;
    private Principal adminPrincipal;
    private AttendanceAdjustment adjustment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(User.Role.EMPLOYEE);

        admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(User.Role.ADMIN);

        userPrincipal = () -> "testuser";
        adminPrincipal = () -> "admin";

        adjustment = new AttendanceAdjustment();
        adjustment.setId(1L);
        adjustment.setUser(user);
        adjustment.setStatus(AttendanceAdjustment.AdjustmentStatus.PENDING);
    }

    @Test
    void testRequestAttendanceAdjustment_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(attendanceAdjustmentRepository.save(any(AttendanceAdjustment.class))).thenReturn(adjustment);

        AttendanceAdjustment result = attendanceAdjustmentService.requestAttendanceAdjustment(
                userPrincipal, LocalDateTime.now(), LocalDateTime.now().plusHours(8), "Forgot to clock in");

        assertNotNull(result);
        assertEquals(AttendanceAdjustment.AdjustmentStatus.PENDING, result.getStatus());
        verify(attendanceAdjustmentRepository, times(1)).save(any(AttendanceAdjustment.class));
    }

    @Test
    void testProcessAttendanceAdjustment_Approve_Success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(attendanceAdjustmentRepository.findById(1L)).thenReturn(Optional.of(adjustment));
        when(attendanceAdjustmentRepository.save(any(AttendanceAdjustment.class))).thenReturn(adjustment);

        AttendanceAdjustment result = attendanceAdjustmentService.processAttendanceAdjustment(
                adminPrincipal, 1L, AttendanceAdjustment.AdjustmentStatus.APPROVED);

        assertNotNull(result);
        assertEquals(AttendanceAdjustment.AdjustmentStatus.APPROVED, result.getStatus());
        verify(attendanceRepository, times(1)).save(any());
        verify(attendanceAdjustmentRepository, times(1)).save(adjustment);
    }

    @Test
    void testProcessAttendanceAdjustment_Reject_Success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(attendanceAdjustmentRepository.findById(1L)).thenReturn(Optional.of(adjustment));
        when(attendanceAdjustmentRepository.save(any(AttendanceAdjustment.class))).thenReturn(adjustment);

        AttendanceAdjustment result = attendanceAdjustmentService.processAttendanceAdjustment(
                adminPrincipal, 1L, AttendanceAdjustment.AdjustmentStatus.REJECTED);

        assertNotNull(result);
        assertEquals(AttendanceAdjustment.AdjustmentStatus.REJECTED, result.getStatus());
        verify(attendanceRepository, never()).save(any());
        verify(attendanceAdjustmentRepository, times(1)).save(adjustment);
    }

    @Test
    void testProcessAttendanceAdjustment_Unauthorized() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedActionException.class, () -> attendanceAdjustmentService.processAttendanceAdjustment(
                userPrincipal, 1L, AttendanceAdjustment.AdjustmentStatus.APPROVED));
    }
    
    @Test
    void testProcessAttendanceAdjustment_AdjustmentNotFound() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(attendanceAdjustmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> attendanceAdjustmentService.processAttendanceAdjustment(
                adminPrincipal, 1L, AttendanceAdjustment.AdjustmentStatus.APPROVED));
    }
} 