package com.example.backend.controller;


import com.example.backend.models.Environment;
import com.example.backend.repo.EnvironmentRepo;
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

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class EnvironmentControllerTest
{

    @Mock
    private EnvironmentRepo environmentRepo;

    @InjectMocks
    private EnvironmentController environmentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Environment testEnvironment;

    @BeforeEach
    void setUp () throws Exception
    {
        mockMvc = MockMvcBuilders.standaloneSetup(environmentController).build();
        objectMapper = new ObjectMapper();

        testEnvironment = new Environment();
        testEnvironment.setId(1L);
        testEnvironment.setName("Test Environment");
        testEnvironment.setUrl(new URL("http://test.example.com"));
        testEnvironment.setUsername("testuser");
        testEnvironment.setPassword("testpass");
        testEnvironment.setProjectID(1L);
    }

    @Test
    void getEnvironmentById_WhenEnvironmentExists_ShouldReturnOkWithEnvironment () throws Exception
    {
        // Given
        when(environmentRepo.findById(1L)).thenReturn(Optional.of(testEnvironment));

        // When & Then
        mockMvc.perform(get("/getEnvironmentById/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Environment"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(environmentRepo, times(1)).findById(1L);
    }

    @Test
    void getEnvironmentById_WhenEnvironmentNotFound_ShouldReturnNotFound () throws Exception
    {
        // Given
        when(environmentRepo.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/getEnvironmentById/1"))
                .andExpect(status().isNotFound());

        verify(environmentRepo, times(1)).findById(1L);
    }

    @Test
    void getEnvironmentById_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        when(environmentRepo.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/getEnvironmentById/1"))
                .andExpect(status().isInternalServerError());

        verify(environmentRepo, times(1)).findById(1L);
    }

    @Test
    void createEnvironment_WithValidData_ShouldReturnCreatedWithEnvironment () throws Exception
    {
        // Given
        Environment newEnvironment = new Environment();
        newEnvironment.setName("New Environment");
        newEnvironment.setUrl(new URL("http://new.example.com"));
        newEnvironment.setUsername("newuser");
        newEnvironment.setPassword("newpass");
        newEnvironment.setProjectID(2L);

        Environment savedEnvironment = new Environment();
        savedEnvironment.setId(2L);
        savedEnvironment.setName("New Environment");
        savedEnvironment.setUrl(new URL("http://new.example.com"));
        savedEnvironment.setUsername("newuser");
        savedEnvironment.setPassword("newpass");
        savedEnvironment.setProjectID(2L);

        when(environmentRepo.save(any(Environment.class))).thenReturn(savedEnvironment);

        // When & Then
        mockMvc.perform(post("/createEnvironment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEnvironment)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("New Environment"))
                .andExpect(jsonPath("$.username").value("newuser"));

        verify(environmentRepo, times(1)).save(any(Environment.class));
    }

    @Test
    void updateEnvironment_WhenEnvironmentExists_ShouldReturnOkWithUpdatedEnvironment () throws Exception
    {
        // Given
        Environment updateRequest = new Environment();
        updateRequest.setName("Updated Environment");
        updateRequest.setUrl(new URL("http://updated.example.com"));
        updateRequest.setUsername("updateduser");
        updateRequest.setPassword("updatedpass");

        Environment updatedEnvironment = new Environment();
        updatedEnvironment.setId(1L);
        updatedEnvironment.setName("Updated Environment");
        updatedEnvironment.setUrl(new URL("http://updated.example.com"));
        updatedEnvironment.setUsername("updateduser");
        updatedEnvironment.setPassword("updatedpass");
        updatedEnvironment.setProjectID(1L);

        when(environmentRepo.findById(1L)).thenReturn(Optional.of(testEnvironment));
        when(environmentRepo.save(any(Environment.class))).thenReturn(updatedEnvironment);

        // When & Then
        mockMvc.perform(patch("/updateEnvironment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Environment"))
                .andExpect(jsonPath("$.username").value("updateduser"));

        verify(environmentRepo, times(1)).findById(1L);
        verify(environmentRepo, times(1)).save(any(Environment.class));
    }

    @Test
    void updateEnvironment_WhenEnvironmentNotFound_ShouldReturnNotFound () throws Exception
    {
        // Given
        Environment updateRequest = new Environment();
        updateRequest.setName("Updated Environment");

        when(environmentRepo.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(patch("/updateEnvironment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(environmentRepo, times(1)).findById(1L);
        verify(environmentRepo, never()).save(any(Environment.class));
    }

    @Test
    void updateEnvironment_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        Environment updateRequest = new Environment();
        updateRequest.setName("Updated Environment");

        when(environmentRepo.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(patch("/updateEnvironment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());

        verify(environmentRepo, times(1)).findById(1L);
    }

    @Test
    void deleteEnvironment_ShouldReturnOk () throws Exception
    {
        // Given
        doNothing().when(environmentRepo).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/deleteEnvironment/1"))
                .andExpect(status().isOk());

        verify(environmentRepo, times(1)).deleteById(1L);
    }

    @Test
    void getEnvironmentsByProjectId_WhenEnvironmentsExist_ShouldReturnOkWithEnvironments () throws Exception
    {
        // Given
        List<Environment> environments = Arrays.asList(testEnvironment);
        when(environmentRepo.findByProjectId(1L)).thenReturn(environments);

        // When & Then
        mockMvc.perform(get("/getEnvironmentsByProjectId/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Environment"))
                .andExpect(jsonPath("$[0].projectID").value(1L));

        verify(environmentRepo, times(1)).findByProjectId(1L);
    }

    @Test
    void getEnvironmentsByProjectId_WhenNoEnvironmentsExist_ShouldReturnNoContent () throws Exception
    {
        // Given
        when(environmentRepo.findByProjectId(1L)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/getEnvironmentsByProjectId/1"))
                .andExpect(status().isNoContent());

        verify(environmentRepo, times(1)).findByProjectId(1L);
    }

    @Test
    void getEnvironmentsByProjectId_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        when(environmentRepo.findByProjectId(anyLong())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/getEnvironmentsByProjectId/1"))
                .andExpect(status().isInternalServerError());

        verify(environmentRepo, times(1)).findByProjectId(1L);
    }

    @Test
    void createEnvironment_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        Environment newEnvironment = new Environment();
        newEnvironment.setName("New Environment");
        newEnvironment.setUrl(new URL("http://new.example.com"));
        newEnvironment.setUsername("newuser");
        newEnvironment.setPassword("newpass");
        newEnvironment.setProjectID(2L);

        when(environmentRepo.save(any(Environment.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/createEnvironment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEnvironment)))
                .andExpect(status().isInternalServerError());

        verify(environmentRepo, times(1)).save(any(Environment.class));
    }

    @Test
    void deleteEnvironment_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        doThrow(new RuntimeException("Database error")).when(environmentRepo).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/deleteEnvironment/1"))
                .andExpect(status().isInternalServerError());

        verify(environmentRepo, times(1)).deleteById(1L);
    }

    @Test
    void updatingEnvironment_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        Environment updateRequest = new Environment();
        updateRequest.setName("Updated Environment");

        when(environmentRepo.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(patch("/updateEnvironment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());

        verify(environmentRepo, times(1)).findById(1L);
    }
}
