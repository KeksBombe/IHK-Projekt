package com.example.backend.controller;


import com.example.backend.dto.CreateProjectRequest;
import com.example.backend.dto.ProjectDto;
import com.example.backend.dto.RenameProjectRequest;
import com.example.backend.mapper.ProjectMapper;
import com.example.backend.models.Project;
import com.example.backend.repo.ProjectRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProjectControllerTest
{

    @Mock
    private ProjectRepo projectRepo;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectController projectController;

    private Project sampleProject;
    private ProjectDto sampleProjectDto;

    @BeforeEach
    void setUp ()
    {
        sampleProject = new Project();
        sampleProject.setId(1L);
        sampleProject.setName("Core Banking");

        sampleProjectDto = new ProjectDto();
        sampleProjectDto.setId(1L);
        sampleProjectDto.setName("Core Banking");
    }

    @Test
    @DisplayName("getAllProjects liefert 200 mit vorhandenen Projekten")
    void getAllProjectsReturnsOkWithProjects ()
    {
        when(projectRepo.findAll()).thenReturn(List.of(sampleProject));
        when(projectMapper.toDto(sampleProject)).thenReturn(sampleProjectDto);

        ResponseEntity<List<ProjectDto>> response = projectController.getAllProjects();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Core Banking");
    }

    @Test
    @DisplayName("getAllProjects liefert 204 wenn keine Projekte existieren")
    void getAllProjectsReturnsNoContentWhenEmpty ()
    {
        when(projectRepo.findAll()).thenReturn(List.of());

        ResponseEntity<List<ProjectDto>> response = projectController.getAllProjects();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("getProjectById liefert 200 wenn Projekt gefunden")
    void getProjectByIdReturnsOkWhenProjectExists ()
    {
        when(projectRepo.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(projectMapper.toDto(sampleProject)).thenReturn(sampleProjectDto);

        ResponseEntity<ProjectDto> response = projectController.getProjectById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        assertEquals("Core Banking", response.getBody().getName());
    }

    @Test
    @DisplayName("createProject speichert neues Projekt mit 201")
    void createProjectReturnsCreated ()
    {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Greenfield");

        Project newProject = new Project();
        newProject.setName("Greenfield");

        Project savedProject = new Project();
        savedProject.setId(99L);
        savedProject.setName("Greenfield");

        ProjectDto savedDto = new ProjectDto();
        savedDto.setId(99L);
        savedDto.setName("Greenfield");

        when(projectMapper.toEntity(request)).thenReturn(newProject);
        when(projectRepo.save(newProject)).thenReturn(savedProject);
        when(projectMapper.toDto(savedProject)).thenReturn(savedDto);

        ResponseEntity<ProjectDto> response = projectController.createProject(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ProjectDto created = response.getBody();
        assertThat(created).isNotNull();
        assertEquals(99L, created.getId());
        assertEquals("Greenfield", created.getName());
    }

    @Test
    @DisplayName("renameProject aktualisiert Namen")
    void renameProjectUpdatesName ()
    {
        RenameProjectRequest request = new RenameProjectRequest();
        request.setName("Updated Name");

        Project existing = new Project();
        existing.setId(1L);
        existing.setName("Old Name");

        Project updated = new Project();
        updated.setId(1L);
        updated.setName("Updated Name");

        ProjectDto updatedDto = new ProjectDto();
        updatedDto.setId(1L);
        updatedDto.setName("Updated Name");

        when(projectRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepo.save(any(Project.class))).thenReturn(updated);
        when(projectMapper.toDto(updated)).thenReturn(updatedDto);

        ResponseEntity<ProjectDto> response = projectController.renameProject(request, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProjectDto result = response.getBody();
        assertThat(result).isNotNull();
        assertEquals("Updated Name", result.getName());
    }

    @Test
    @DisplayName("deleteProject entfernt Projekt erfolgreich")
    void deleteProjectReturnsOkOnSuccess ()
    {
        when(projectRepo.existsById(1L)).thenReturn(true);

        ResponseEntity<Void> response = projectController.deleteProject(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(projectRepo, times(1)).deleteById(1L);
    }
}