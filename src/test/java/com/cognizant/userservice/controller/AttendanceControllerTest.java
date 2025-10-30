package com.cognizant.userservice.controller;

import com.cognizant.userservice.model.Attendance;
import com.cognizant.userservice.service.AttendanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    private Attendance attendance;
    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController).build();
        attendance = new Attendance();
        attendance.setId(1L);
        attendance.setCheckInTime(LocalDateTime.now());
        principal = () -> "testuser";
    }

    @Test
    void testCheckIn() throws Exception {
        when(attendanceService.checkIn(any(Principal.class))).thenReturn(attendance);

        mockMvc.perform(post("/api/v1/attendance/checkin").principal(principal))
                .andExpect(status().isCreated());
    }

    @Test
    void testCheckOut() throws Exception {
        attendance.setCheckOutTime(LocalDateTime.now().plusHours(8));
        when(attendanceService.checkOut(any(Principal.class))).thenReturn(attendance);

        mockMvc.perform(put("/api/v1/attendance/checkout").principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllAttendancesForUser_AsAdmin() throws Exception {
        when(attendanceService.getAttendanceDataForUser(1L)).thenReturn(Collections.singletonList(attendance));

        mockMvc.perform(get("/api/v1/attendance/user/1/all"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyAllAttendances() throws Exception {
        when(attendanceService.getAllAttendancesForLoggedInUser(any(Principal.class))).thenReturn(Collections.singletonList(attendance));

        mockMvc.perform(get("/api/v1/attendance/my-all").principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void testGetWeeklyStatsForUser() throws Exception {
        when(attendanceService.getWeeklyStats(anyLong(), anyInt(), anyInt())).thenReturn(new HashMap<>());
        mockMvc.perform(get("/api/v1/attendance/user/1/stats/weekly")
                        .param("year", "2023")
                        .param("weekOfYear", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyWeeklyStats() throws Exception {
        when(attendanceService.getMyWeeklyStats(any(Principal.class), anyInt(), anyInt())).thenReturn(new HashMap<>());
        mockMvc.perform(get("/api/v1/attendance/my-stats/weekly")
                        .principal(principal)
                        .param("year", "2023")
                        .param("weekOfYear", "1"))
                .andExpect(status().isOk());
    }
} 