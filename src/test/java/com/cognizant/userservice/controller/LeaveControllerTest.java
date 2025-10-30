package com.cognizant.userservice.controller;

import com.cognizant.userservice.model.Leave;
import com.cognizant.userservice.service.LeaveService;
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
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeaveControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LeaveService leaveService;

    @InjectMocks
    private LeaveController leaveController;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Leave leave;
    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(leaveController).build();
        leave = new Leave();
        leave.setId(1L);
        leave.setStartDate(LocalDate.now().plusDays(1));
        leave.setEndDate(LocalDate.now().plusDays(2));
        leave.setStatus(Leave.LeaveStatus.PENDING);
        principal = () -> "testuser";
    }

    @Test
    void testApplyLeave() throws Exception {
        when(leaveService.applyLeave(any(Leave.class), anyString())).thenReturn(leave);

        mockMvc.perform(post("/api/v1/leaves/apply")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leave)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyLeaves() throws Exception {
        when(leaveService.getLeavesByUser(anyString())).thenReturn(Collections.singletonList(leave));

        mockMvc.perform(get("/api/v1/leaves/my-leaves").principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPendingLeaves() throws Exception {
        when(leaveService.getPendingLeaves()).thenReturn(Collections.singletonList(leave));

        mockMvc.perform(get("/api/v1/leaves/pending"))
                .andExpect(status().isOk());
    }

    @Test
    void testProcessLeaveRequest() throws Exception {
        when(leaveService.processLeaveRequest(anyLong(), any(Leave.LeaveStatus.class), anyString())).thenReturn(leave);

        mockMvc.perform(put("/api/v1/leaves/1")
                        .param("status", "APPROVED")
                        .param("adminComment", "Approved"))
                .andExpect(status().isOk());
    }
} 