package com.example.backend.controller;


import com.example.backend.models.Environment;
import com.example.backend.models.Project;
import com.example.backend.repo.EnvironmentRepo;
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


@ExtendWith(MockitoExtension.class)
class EnvironmentControllerTest
{

    private static final String MASKED_PASSWORD = "************";

    @Mock
    private EnvironmentRepo environmentRepo;

    @InjectMocks
    private EnvironmentController environmentController;

    private Environment storedEnvironment;

    @BeforeEach
    void setUp () throws Exception
    {
        storedEnvironment = buildEnvironment(1L, "QA", "qa_user", "secret", uriToUrl("https://qa.example.com"));
    }

    @Test
    @DisplayName("getEnvironmentById liefert maskiertes Environment")
    void getEnvironmentByIdReturnsMaskedEnvironment ()
    {
        when(environmentRepo.findById(1L)).thenReturn(Optional.of(storedEnvironment));

        ResponseEntity<Environment> response = environmentController.getEnvironmentById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Environment body = Objects.requireNonNull(response.getBody());
        assertThat(body).isNotNull();
        assertEquals(MASKED_PASSWORD, body.getPassword());
        assertEquals(storedEnvironment.getUsername(), body.getUsername());
    }

    @Test
    @DisplayName("getEnvironmentById liefert 404 wenn Environment fehlt")
    void getEnvironmentByIdReturnsNotFoundWhenMissing ()
    {
        when(environmentRepo.findById(5L)).thenReturn(Optional.empty());

        ResponseEntity<Environment> response = environmentController.getEnvironmentById(5L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("getEnvironmentById liefert 500 bei Fehler")
    void getEnvironmentByIdReturnsInternalServerErrorOnException ()
    {
        when(environmentRepo.findById(2L)).thenThrow(new RuntimeException("db"));

        ResponseEntity<Environment> response = environmentController.getEnvironmentById(2L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("createEnvironment setzt ID auf null und maskiert Passwort")
    void createEnvironmentResetsIdAndMasksPassword () throws Exception
    {
        Environment request = buildEnvironment(77L, "Prod", "deploy", "new_secret", uriToUrl("https://prod.example.com"));
        when(environmentRepo.save(any(Environment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Environment> response = environmentController.createEnvironment(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Environment body = Objects.requireNonNull(response.getBody());
        assertThat(body).isNotNull();
        assertEquals(MASKED_PASSWORD, body.getPassword());

        ArgumentCaptor<Environment> captor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepo).save(captor.capture());
        assertNull(captor.getValue().getId());
    }

    @Test
    @DisplayName("createEnvironment liefert 500 bei Fehler")
    void createEnvironmentReturnsInternalServerErrorOnException ()
    {
        when(environmentRepo.save(any(Environment.class))).thenThrow(new RuntimeException("duplicate"));

        ResponseEntity<Environment> response = environmentController.createEnvironment(new Environment());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("updateEnvironment ersetzt Passwort wenn neues Passwort gesendet wird")
    void updateEnvironmentUpdatesPasswordWhenProvided () throws Exception
    {
        Environment updateRequest = buildEnvironment(null, "QA-2", "qa_new", "new-pass", uriToUrl("https://qa2.example.com"));
        when(environmentRepo.findById(1L)).thenReturn(Optional.of(storedEnvironment));
        when(environmentRepo.save(any(Environment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Environment> response = environmentController.updateEnvironment(updateRequest, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Environment body = Objects.requireNonNull(response.getBody());
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
        Environment updateRequest = new Environment();
        updateRequest.setPassword(MASKED_PASSWORD);
        updateRequest.setName("QA");
        updateRequest.setUsername("qa_user");

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

        ResponseEntity<Environment> response = environmentController.updateEnvironment(new Environment(), 9L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("updateEnvironment liefert 500 bei Fehler")
    void updateEnvironmentReturnsInternalServerErrorOnException ()
    {
        when(environmentRepo.findById(4L)).thenThrow(new RuntimeException("timeout"));

        ResponseEntity<Environment> response = environmentController.updateEnvironment(new Environment(), 4L);

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

        ResponseEntity<List<Environment>> response = environmentController.getEnvironmentsByProjectId(3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Environment> environments = Objects.requireNonNull(response.getBody());
        assertThat(environments).isNotNull();
        assertThat(environments).hasSize(1);
        assertEquals(MASKED_PASSWORD, environments.get(0).getPassword());
    }

    @Test
    @DisplayName("getEnvironmentsByProjectId liefert 204 wenn keine Environments existieren")
    void getEnvironmentsByProjectIdReturnsNoContentWhenEmpty ()
    {
        when(environmentRepo.findByProjectId(6L)).thenReturn(List.of());

        ResponseEntity<List<Environment>> response = environmentController.getEnvironmentsByProjectId(6L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("getEnvironmentsByProjectId liefert 500 bei Fehler")
    void getEnvironmentsByProjectIdReturnsInternalServerErrorOnException ()
    {
        when(environmentRepo.findByProjectId(7L)).thenThrow(new RuntimeException("sql"));

        ResponseEntity<List<Environment>> response = environmentController.getEnvironmentsByProjectId(7L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private Environment buildEnvironment (Long id, String name, String username, String password, URL url)
    {
        Environment environment = new Environment();
        environment.setId(id);
        environment.setName(name);
        environment.setUsername(username);
        environment.setPassword(password);
        environment.setUrl(url);
        Project project = new Project();
        project.setId(10L);
        environment.setProject(project);
        return environment;
    }

    private URL uriToUrl (String value) throws Exception
    {
        return URI.create(value).toURL();
    }
}
