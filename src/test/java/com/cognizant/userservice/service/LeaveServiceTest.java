package com.cognizant.userservice.service;

import com.cognizant.userservice.exception.InvalidLeaveRequestException;
import com.cognizant.userservice.model.Leave;
import com.cognizant.userservice.model.User;
import com.cognizant.userservice.repository.LeaveRepository;
import com.cognizant.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaveService leaveService;

    private User user;
    private Leave leave;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        leave = new Leave();
        leave.setId(1L);
        leave.setUser(user);
        leave.setStartDate(LocalDate.now().plusDays(1));
        leave.setEndDate(LocalDate.now().plusDays(2));
    }

    @Test
    void testApplyLeave_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(leaveRepository.save(any(Leave.class))).thenReturn(leave);

        Leave newLeave = new Leave();
        newLeave.setStartDate(LocalDate.now().plusDays(1));
        newLeave.setEndDate(LocalDate.now().plusDays(2));

        Leave appliedLeave = leaveService.applyLeave(newLeave, "testuser");

        assertNotNull(appliedLeave);
        assertEquals(Leave.LeaveStatus.PENDING, appliedLeave.getStatus());
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    void testApplyLeave_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        Leave newLeave = new Leave();
        newLeave.setStartDate(LocalDate.now().plusDays(1));
        newLeave.setEndDate(LocalDate.now().plusDays(2));

        assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(newLeave, "testuser"));
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void testApplyLeave_InvalidDate() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Leave newLeave = new Leave();
        newLeave.setStartDate(LocalDate.now().minusDays(1)); // Past date
        newLeave.setEndDate(LocalDate.now().plusDays(1));

        assertThrows(InvalidLeaveRequestException.class, () -> leaveService.applyLeave(newLeave, "testuser"));
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void testProcessLeaveRequest_Success() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any(Leave.class))).thenReturn(leave);

        Leave processedLeave = leaveService.processLeaveRequest(1L, Leave.LeaveStatus.APPROVED, "Approved");

        assertNotNull(processedLeave);
        assertEquals(Leave.LeaveStatus.APPROVED, processedLeave.getStatus());
        assertEquals("Approved", processedLeave.getAdminComment());
        verify(leaveRepository, times(1)).save(leave);
    }

    @Test
    void testProcessLeaveRequest_NotFound() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> leaveService.processLeaveRequest(1L, Leave.LeaveStatus.APPROVED, "Approved"));
        verify(leaveRepository, never()).save(any(Leave.class));
    }
} 