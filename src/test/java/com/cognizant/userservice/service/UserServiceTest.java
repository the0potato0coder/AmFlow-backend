package com.cognizant.userservice.service;

import com.cognizant.userservice.exception.UserNotFoundException;
import com.cognizant.userservice.model.User;
import com.cognizant.userservice.repository.AttendanceAdjustmentRepository;
import com.cognizant.userservice.repository.AttendanceRepository;
import com.cognizant.userservice.repository.LeaveRepository;
import com.cognizant.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceAdjustmentRepository attendanceAdjustmentRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
    }

    @Test
    void testFindAll() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        List<User> users = userService.findAll();
        assertNotNull(users);
        assertEquals(1, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User foundUser = userService.findById(1L);
        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findById(1L));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        User foundUser = userService.findByUsername("testuser");
        assertNotNull(foundUser);
        assertEquals(user.getUsername(), foundUser.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testFindByUsername_NotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findByUsername("testuser"));
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testSave_Success() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setPassword("password");
        User savedUser = userService.save(newUser);

        assertNotNull(savedUser);
        assertEquals(user.getUsername(), savedUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSave_UsernameAlreadyExists() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        assertThrows(com.cognizant.userservice.exception.UsernameAlreadyExistsException.class, () -> userService.save(user));
        verify(userRepository, never()).save(user);
    }

    @Test
    void testDeleteById_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        doNothing().when(leaveRepository).deleteByUserId(1L);
        doNothing().when(attendanceRepository).deleteByUserId(1L);
        doNothing().when(attendanceAdjustmentRepository).deleteByUserId(1L);

        userService.deleteById(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
        verify(leaveRepository, times(1)).deleteByUserId(1L);
        verify(attendanceRepository, times(1)).deleteByUserId(1L);
        verify(attendanceAdjustmentRepository, times(1)).deleteByUserId(1L);
    }

    @Test
    void testDeleteById_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> userService.deleteById(1L));
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, never()).deleteById(1L);
    }
} 