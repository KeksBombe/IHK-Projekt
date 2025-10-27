package com.example.backend.controller;


import com.example.backend.dto.EnvironmentDto;
import com.example.backend.mapper.EnvironmentMapper;
import com.example.backend.models.Environment;
import com.example.backend.models.Project;
import com.example.backend.repo.EnvironmentRepo;
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

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
class EnvironmentControllerTest
{

    private static final String MASKED_PASSWORD = "************";

    @Mock
    private EnvironmentRepo environmentRepo;

    @Mock
    private ProjectRepo projectRepo;

    @Mock
    private EnvironmentMapper environmentMapper;

    @InjectMocks
    private EnvironmentController environmentController;

    private Environment storedEnvironment;

    @BeforeEach
    void setUp () throws Exception
    {
        storedEnvironment = buildEnvironment(1L, "QA", "qa_user", "secret", uriToUrl("https://qa.example.com"));

        // Default mapping behavior for mapper used by controller
        lenient().when(environmentMapper.toDto(any(Environment.class))).thenAnswer(invocation ->
        {
            Environment env = invocation.getArgument(0);
            if (env == null) return null;
            EnvironmentDto dto = new EnvironmentDto();
            dto.setId(env.getId());
            dto.setName(env.getName());
            dto.setUsername(env.getUsername());
            dto.setPassword(env.getPassword());
            dto.setUrl(env.getUrl());
            dto.setProjectID(env.getProject() != null ? env.getProject().getId() : null);
            return dto;
        });

        lenient().when(environmentMapper.toEntity(any(EnvironmentDto.class))).thenAnswer(invocation ->
        {
            EnvironmentDto dto = invocation.getArgument(0);
            if (dto == null) return null;
            Environment env = new Environment();
            env.setId(dto.getId());
            env.setName(dto.getName());
            env.setUsername(dto.getUsername());
            env.setPassword(dto.getPassword());
            env.setUrl(dto.getUrl());
            if (dto.getProjectID() != null)
            {
                Project p = new Project();
                p.setId(dto.getProjectID());
                env.setProject(p);
            }
            return env;
        });
    }

    @Test
    @DisplayName("getEnvironmentById liefert maskiertes Environment")
    void getEnvironmentByIdReturnsMaskedEnvironment ()
    {
        when(environmentRepo.findById(1L)).thenReturn(Optional.of(storedEnvironment));

        ResponseEntity<EnvironmentDto> response = environmentController.getEnvironmentById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        EnvironmentDto body = Objects.requireNonNull(response.getBody());
        assertThat(body).isNotNull();
        assertEquals(MASKED_PASSWORD, body.getPassword());
        assertEquals(storedEnvironment.getUsername(), body.getUsername());
    }

    @Test
    @DisplayName("getEnvironmentById liefert 404 wenn Environment fehlt")
    void getEnvironmentByIdReturnsNotFoundWhenMissing ()
    {
        when(environmentRepo.findById(5L)).thenReturn(Optional.empty());

        ResponseEntity<EnvironmentDto> response = environmentController.getEnvironmentById(5L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("getEnvironmentById liefert 500 bei Fehler")
    void getEnvironmentByIdReturnsInternalServerErrorOnException ()
    {
        when(environmentRepo.findById(2L)).thenThrow(new RuntimeException("db"));

        ResponseEntity<EnvironmentDto> response = environmentController.getEnvironmentById(2L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("createEnvironment setzt ID auf null und maskiert Passwort")
    void createEnvironmentResetsIdAndMasksPassword () throws Exception
    {
        EnvironmentDto requestDto = new EnvironmentDto();
        requestDto.setId(77L);
        requestDto.setName("Prod");
        requestDto.setUsername("deploy");
        requestDto.setPassword("new_secret");
        requestDto.setUrl("https://prod.example.com");
        requestDto.setProjectID(10L);

        Project project = new Project();
        project.setId(10L);

        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(environmentRepo.save(any(Environment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<EnvironmentDto> response = environmentController.createEnvironment(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        EnvironmentDto body = Objects.requireNonNull(response.getBody());
        assertThat(body).isNotNull();
        assertEquals(MASKED_PASSWORD, body.getPassword());

        ArgumentCaptor<Environment> captor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepo).save(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals(10L, captor.getValue().getProject().getId());
    }

    @Test
    @DisplayName("createEnvironment liefert 500 bei Fehler")
    void createEnvironmentReturnsInternalServerErrorOnException ()
    {
        EnvironmentDto requestDto = new EnvironmentDto();
        requestDto.setProjectID(10L);

        Project project = new Project();
        project.setId(10L);
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(environmentRepo.save(any(Environment.class))).thenThrow(new RuntimeException("duplicate"));

        ResponseEntity<EnvironmentDto> response = environmentController.createEnvironment(requestDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("createEnvironment liefert 400 wenn Projekt nicht gefunden wird")
    void createEnvironmentReturnsBadRequestWhenProjectNotFound ()
    {
        EnvironmentDto requestDto = new EnvironmentDto();
        requestDto.setId(77L);
        requestDto.setName("Prod");
        requestDto.setUsername("deploy");
        requestDto.setPassword("new_secret");
        requestDto.setUrl("https://prod.example.com");
        requestDto.setProjectID(999L);

        when(projectRepo.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<EnvironmentDto> response = environmentController.createEnvironment(requestDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(environmentRepo, times(0)).save(any(Environment.class));
    }

    @Test
    @DisplayName("updateEnvironment ersetzt Passwort wenn neues Passwort gesendet wird")
    void updateEnvironmentUpdatesPasswordWhenProvided () throws Exception
    {
        EnvironmentDto updateRequest = buildEnvironmentDto(null, "QA-2", "qa_new", "new-pass", "https://qa2.example.com", 10L);
        when(environmentRepo.findById(1L)).thenReturn(Optional.of(storedEnvironment));
        when(environmentRepo.save(any(Environment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<EnvironmentDto> response = environmentController.updateEnvironment(updateRequest, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        EnvironmentDto body = Objects.requireNonNull(response.getBody());
        assertThat(body).isNotNull();
        assertEquals(MASKED_PASSWORD, body.getPassword());

        ArgumentCaptor<Environment> captor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepo).save(captor.capture());
        assertEquals("new-pass", captor.getValue().getPassword());
        assertEquals("QA-2", captor.getValue().getName());
    }

    @Test
    @DisplayName("updateEnvironment behält altes Passwort wenn Maskenwert geschickt wird")
    void updateEnvironmentKeepsPasswordWhenMaskProvided ()
    {
        EnvironmentDto updateRequest = new EnvironmentDto();
        updateRequest.setPassword(MASKED_PASSWORD);
        updateRequest.setName("QA");
        updateRequest.setUsername("qa_user");
        updateRequest.setProjectID(10L);

        when(environmentRepo.findById(1L)).thenReturn(Optional.of(storedEnvironment));
        when(environmentRepo.save(any(Environment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        environmentController.updateEnvironment(updateRequest, 1L);

        ArgumentCaptor<Environment> captor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepo).save(captor.capture());
        assertEquals("secret", captor.getValue().getPassword());
    }

    @Test
    @DisplayName("updateEnvironment liefert 404 wenn Environment fehlt")
    void updateEnvironmentReturnsNotFoundWhenMissing ()
    {
        when(environmentRepo.findById(9L)).thenReturn(Optional.empty());

        ResponseEntity<EnvironmentDto> response = environmentController.updateEnvironment(new EnvironmentDto(), 9L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("updateEnvironment liefert 500 bei Fehler")
    void updateEnvironmentReturnsInternalServerErrorOnException ()
    {
        when(environmentRepo.findById(4L)).thenThrow(new RuntimeException("timeout"));

        ResponseEntity<EnvironmentDto> response = environmentController.updateEnvironment(new EnvironmentDto(), 4L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("deleteEnvironment entfernt Environment erfolgreich")
    void deleteEnvironmentReturnsOkOnSuccess ()
    {
        ResponseEntity<Void> response = environmentController.deleteEnvironment(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(environmentRepo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteEnvironment liefert 500 bei Fehler")
    void deleteEnvironmentReturnsInternalServerErrorOnException ()
    {
        doThrow(new RuntimeException("locked")).when(environmentRepo).deleteById(8L);

        ResponseEntity<Void> response = environmentController.deleteEnvironment(8L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("getEnvironmentsByProjectId liefert maskierte Liste")
    void getEnvironmentsByProjectIdReturnsMaskedEnvironments ()
    {
        when(environmentRepo.findByProjectId(3L)).thenReturn(List.of(storedEnvironment));

        ResponseEntity<List<EnvironmentDto>> response = environmentController.getEnvironmentsByProjectId(3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<EnvironmentDto> environments = Objects.requireNonNull(response.getBody());
        assertThat(environments).isNotNull();
        assertThat(environments).hasSize(1);
        assertEquals(MASKED_PASSWORD, environments.get(0).getPassword());
    }

    @Test
    @DisplayName("getEnvironmentsByProjectId liefert 204 wenn keine Environments existieren")
    void getEnvironmentsByProjectIdReturnsNoContentWhenEmpty ()
    {
        when(environmentRepo.findByProjectId(6L)).thenReturn(List.of());

        ResponseEntity<List<EnvironmentDto>> response = environmentController.getEnvironmentsByProjectId(6L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("getEnvironmentsByProjectId liefert 500 bei Fehler")
    void getEnvironmentsByProjectIdReturnsInternalServerErrorOnException ()
    {
        when(environmentRepo.findByProjectId(7L)).thenThrow(new RuntimeException("sql"));

        ResponseEntity<List<EnvironmentDto>> response = environmentController.getEnvironmentsByProjectId(7L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private Environment buildEnvironment (Long id, String name, String username, String password, URL url)
    {
        Environment environment = new Environment();
        environment.setId(id);
        environment.setName(name);
        environment.setUsername(username);
        environment.setPassword(password);
        environment.setUrl(url.toString());
        Project project = new Project();
        project.setId(10L);
        environment.setProject(project);
        return environment;
    }

    private EnvironmentDto buildEnvironmentDto (Long id, String name, String username, String password, String url, Long projectId)
    {
        EnvironmentDto dto = new EnvironmentDto();
        dto.setId(id);
        dto.setName(name);
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setUrl(url);
        dto.setProjectID(projectId);
        return dto;
    }

    private URL uriToUrl (String value) throws Exception
    {
        return URI.create(value).toURL();
    }
}
