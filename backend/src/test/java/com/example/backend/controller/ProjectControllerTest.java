package com.example.backend.controller;


import com.example.backend.models.Project;
import com.example.backend.repo.ProjectRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ProjectControllerTest
{

    @Mock
    private ProjectRepo projectRepo;

    @InjectMocks
    private ProjectController projectController;

    private Project sampleProject;

    @BeforeEach
    void setUp ()
    {
        sampleProject = new Project();
        sampleProject.setId(1L);
        sampleProject.setName("Core Banking");
    }

    @Test
    @DisplayName("getAllProjects liefert 200 mit vorhandenen Projekten")
    void getAllProjectsReturnsOkWithProjects ()
    {
        when(projectRepo.findAll()).thenReturn(List.of(sampleProject));

        ResponseEntity<List<Project>> response = projectController.getAllProjects();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).containsExactly(sampleProject);
    }

    @Test
    @DisplayName("getAllProjects liefert 204 wenn keine Projekte existieren")
    void getAllProjectsReturnsNoContentWhenEmpty ()
    {
        when(projectRepo.findAll()).thenReturn(List.of());

        ResponseEntity<List<Project>> response = projectController.getAllProjects();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("getAllProjects liefert 500 bei Repository Fehler")
    void getAllProjectsReturnsInternalServerErrorOnException ()
    {
        when(projectRepo.findAll()).thenThrow(new RuntimeException("DB offline"));

        ResponseEntity<List<Project>> response = projectController.getAllProjects();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("getProjectById liefert 200 wenn Projekt gefunden")
    void getProjectByIdReturnsOkWhenProjectExists ()
    {
        when(projectRepo.existsById(1L)).thenReturn(true);
        when(projectRepo.findById(1L)).thenReturn(Optional.of(sampleProject));

        ResponseEntity<Project> response = projectController.getProjectById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(sampleProject);
    }

    @Test
    @DisplayName("getProjectById liefert 204 wenn Projekt fehlt")
    void getProjectByIdReturnsNoContentWhenProjectMissing ()
    {
        when(projectRepo.existsById(5L)).thenReturn(false);

        ResponseEntity<Project> response = projectController.getProjectById(5L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("getProjectById liefert 500 bei Repository Fehler")
    void getProjectByIdReturnsInternalServerErrorOnException ()
    {
        when(projectRepo.existsById(2L)).thenThrow(new RuntimeException("Failure"));

        ResponseEntity<Project> response = projectController.getProjectById(2L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("createProject speichert neues Projekt mit 201")
    void createProjectReturnsCreated ()
    {
        Project newProject = new Project();
        newProject.setName("Greenfield");
        when(projectRepo.save(any(Project.class))).thenAnswer(invocation ->
        {
            Project toSave = invocation.getArgument(0);
            toSave.setId(99L);
            return toSave;
        });

        ResponseEntity<Project> response = projectController.createProject(newProject);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Project created = response.getBody();
        assertThat(created).isNotNull();
        assertEquals(99L, created.getId());
    }

    @Test
    @DisplayName("createProject liefert 500 bei Fehler")
    void createProjectReturnsInternalServerErrorOnException ()
    {
        when(projectRepo.save(any(Project.class))).thenThrow(new RuntimeException("Duplicate"));

        ResponseEntity<Project> response = projectController.createProject(new Project());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("renameProject akzeptiert leeren Namen und speichert")
    void renameProjectAcceptsEmptyName ()
    {
        Project request = new Project();
        request.setName("");

        Project persisted = new Project();
        persisted.setId(1L);
        persisted.setName("Legacy");

        when(projectRepo.findById(1L)).thenReturn(Optional.of(persisted));
        when(projectRepo.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Project> response = projectController.updateProject(request, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Project updated = response.getBody();
        assertThat(updated).isNotNull();
        assertEquals("", updated.getName());

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepo).save(captor.capture());
        assertEquals("", captor.getValue().getName());
    }

    @Test
    @DisplayName("renameProject liefert 404 wenn Projekt fehlt")
    void renameProjectReturnsNotFoundWhenMissing ()
    {
        when(projectRepo.findById(3L)).thenReturn(Optional.empty());

        ResponseEntity<Project> response = projectController.updateProject(new Project(), 3L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("renameProject liefert 500 bei Fehler")
    void renameProjectReturnsInternalServerErrorOnException ()
    {
        when(projectRepo.findById(4L)).thenThrow(new RuntimeException("Query failed"));

        ResponseEntity<Project> response = projectController.updateProject(new Project(), 4L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("deleteProject entfernt Projekt erfolgreich")
    void deleteProjectReturnsOkOnSuccess ()
    {
        ResponseEntity<HttpStatus> response = projectController.deleteProject(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(projectRepo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteProject liefert 500 bei Fehler")
    void deleteProjectReturnsInternalServerErrorOnException ()
    {
        doThrow(new RuntimeException("Cannot delete")).when(projectRepo).deleteById(5L);

        ResponseEntity<HttpStatus> response = projectController.deleteProject(5L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
