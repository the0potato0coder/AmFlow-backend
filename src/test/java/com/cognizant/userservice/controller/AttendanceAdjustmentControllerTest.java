package com.cognizant.userservice.controller;

import com.cognizant.userservice.model.AttendanceAdjustment;
import com.cognizant.userservice.service.AttendanceAdjustmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AttendanceAdjustmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceAdjustmentService attendanceAdjustmentService;

    @InjectMocks
    private AttendanceAdjustmentController attendanceAdjustmentController;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private AttendanceAdjustment adjustment;
    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceAdjustmentController).build();
        adjustment = new AttendanceAdjustment();
        adjustment.setId(1L);
        adjustment.setStatus(AttendanceAdjustment.AdjustmentStatus.PENDING);
        principal = () -> "testuser";
    }

    @Test
    void testRequestAdjustment() throws Exception {
        when(attendanceAdjustmentService.requestAttendanceAdjustment(any(Principal.class), any(LocalDateTime.class), any(LocalDateTime.class), any(String.class)))
                .thenReturn(adjustment);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("requestedCheckIn", LocalDateTime.now().toString());
        requestBody.put("requestedCheckOut", LocalDateTime.now().plusHours(8).toString());
        requestBody.put("reason", "Forgot to clock in");

        mockMvc.perform(post("/api/v1/attendance/adjustments/request")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPendingAdjustments() throws Exception {
        when(attendanceAdjustmentService.getPendingAdjustments()).thenReturn(Collections.singletonList(adjustment));

        mockMvc.perform(get("/api/v1/attendance/adjustments/pending"))
                .andExpect(status().isOk());
    }

    @Test
    void testApproveAdjustment() throws Exception {
        when(attendanceAdjustmentService.processAttendanceAdjustment(any(Principal.class), anyLong(), any(AttendanceAdjustment.AdjustmentStatus.class)))
                .thenReturn(adjustment);

        mockMvc.perform(put("/api/v1/attendance/adjustments/1/approve").principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void testRejectAdjustment() throws Exception {
        when(attendanceAdjustmentService.processAttendanceAdjustment(any(Principal.class), anyLong(), any(AttendanceAdjustment.AdjustmentStatus.class)))
                .thenReturn(adjustment);

        mockMvc.perform(put("/api/v1/attendance/adjustments/1/reject").principal(principal))
                .andExpect(status().isOk());
    }
} 