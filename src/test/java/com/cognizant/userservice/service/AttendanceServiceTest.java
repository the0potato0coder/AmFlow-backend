package com.cognizant.userservice.service;

import com.cognizant.userservice.exception.ActiveAttendanceExistsException;
import com.cognizant.userservice.exception.NoActiveAttendanceException;
import com.cognizant.userservice.model.Attendance;
import com.cognizant.userservice.model.User;
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
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private User user;
    private Principal principal;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        principal = () -> "testuser";
    }

    @Test
    void testCheckIn_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(attendanceRepository.findByUserAndCheckOutTimeIsNull(user)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArguments()[0]);

        Attendance attendance = attendanceService.checkIn(principal);

        assertNotNull(attendance);
        assertNotNull(attendance.getCheckInTime());
        assertNull(attendance.getCheckOutTime());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testCheckIn_AlreadyCheckedIn() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(attendanceRepository.findByUserAndCheckOutTimeIsNull(user)).thenReturn(Optional.of(new Attendance()));

        assertThrows(ActiveAttendanceExistsException.class, () -> attendanceService.checkIn(principal));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testCheckOut_Success() {
        Attendance checkedInAttendance = new Attendance();
        checkedInAttendance.setUser(user);
        checkedInAttendance.setCheckInTime(LocalDateTime.now().minusHours(1));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(attendanceRepository.findByUserAndCheckOutTimeIsNull(user)).thenReturn(Optional.of(checkedInAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArguments()[0]);

        Attendance attendance = attendanceService.checkOut(principal);

        assertNotNull(attendance);
        assertNotNull(attendance.getCheckOutTime());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testCheckOut_NotCheckedIn() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(attendanceRepository.findByUserAndCheckOutTimeIsNull(user)).thenReturn(Optional.empty());

        assertThrows(NoActiveAttendanceException.class, () -> attendanceService.checkOut(principal));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }
} 