package com.example.backend.controller;


import com.example.backend.models.Project;
import com.example.backend.repo.ProjectRepo;
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
class ProjectControllerTest
{

    @Mock
    private ProjectRepo projectRepo;

    @InjectMocks
    private ProjectController projectController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Project testProject;

    @BeforeEach
    void setUp ()
    {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();
        objectMapper = new ObjectMapper();

        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
    }

    @Test
    void getAllProjects_WhenProjectsExist_ShouldReturnOkWithProjects () throws Exception
    {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepo.findAll()).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/getAllProjects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Project"));

        verify(projectRepo, times(1)).findAll();
    }

    @Test
    void getAllProjects_WhenNoProjectsExist_ShouldReturnNoContent () throws Exception
    {
        // Given
        when(projectRepo.findAll()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/getAllProjects"))
                .andExpect(status().isNoContent());

        verify(projectRepo, times(1)).findAll();
    }

    @Test
    void getAllProjects_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        when(projectRepo.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/getAllProjects"))
                .andExpect(status().isInternalServerError());

        verify(projectRepo, times(1)).findAll();
    }

    @Test
    void getProjectById_WhenProjectExists_ShouldReturnOkWithProject () throws Exception
    {
        // Given
        when(projectRepo.findById(1L)).thenReturn(Optional.of(testProject));

        // When & Then
        mockMvc.perform(get("/getProjectById/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Project"));

        verify(projectRepo, times(1)).findById(1L);
    }

    @Test
    void getProjectById_WhenProjectNotFound_ShouldReturnNotFound () throws Exception
    {
        // Given
        when(projectRepo.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/getProjectById/1"))
                .andExpect(status().isNotFound());

        verify(projectRepo, times(1)).findById(1L);
    }

    @Test
    void getProjectById_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        when(projectRepo.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/getProjectById/1"))
                .andExpect(status().isInternalServerError());

        verify(projectRepo, times(1)).findById(1L);
    }

    @Test
    void createProject_WithValidData_ShouldReturnCreatedWithProject () throws Exception
    {
        // Given
        Project newProject = new Project("New Project");

        Project savedProject = new Project("New Project");
        savedProject.setId(2L);

        when(projectRepo.save(any(Project.class))).thenReturn(savedProject);

        // When & Then
        mockMvc.perform(post("/createProject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProject)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("New Project"));

        verify(projectRepo, times(1)).save(any(Project.class));
    }

    @Test
    void createProject_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        Project newProject = new Project("New Project");
        when(projectRepo.save(any(Project.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/createProject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProject)))
                .andExpect(status().isInternalServerError());

        verify(projectRepo, times(1)).save(any(Project.class));
    }

    @Test
    void updateProject_WhenProjectExists_ShouldReturnOkWithUpdatedProject () throws Exception
    {
        // Given
        Project updateRequest = new Project("Updated Project");

        Project updatedProject = new Project("Updated Project");
        updatedProject.setId(1L);

        when(projectRepo.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepo.save(any(Project.class))).thenReturn(updatedProject);

        // When & Then
        mockMvc.perform(patch("/renameProject/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Project"));

        verify(projectRepo, times(1)).findById(1L);
        verify(projectRepo, times(1)).save(any(Project.class));
    }

    @Test
    void updateProject_WhenProjectNotFound_ShouldReturnNotFound () throws Exception
    {
        // Given
        Project updateRequest = new Project("Updated Project");
        when(projectRepo.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(patch("/renameProject/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(projectRepo, times(1)).findById(1L);
        verify(projectRepo, never()).save(any(Project.class));
    }

    @Test
    void updateProject_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        Project updateRequest = new Project("Updated Project");
        when(projectRepo.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(patch("/renameProject/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());

        verify(projectRepo, times(1)).findById(1L);
    }

    @Test
    void deleteProject_ShouldReturnOk () throws Exception
    {
        // Given
        doNothing().when(projectRepo).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/deleteProject/1"))
                .andExpect(status().isOk());

        verify(projectRepo, times(1)).deleteById(1L);
    }

    @Test
    void deleteProject_WhenExceptionOccurs_ShouldReturnInternalServerError () throws Exception
    {
        // Given
        doThrow(new RuntimeException("Database error")).when(projectRepo).deleteById(anyLong());

        // When & Then
        mockMvc.perform(delete("/deleteProject/1"))
                .andExpect(status().isInternalServerError());

        verify(projectRepo, times(1)).deleteById(1L);
    }
}
