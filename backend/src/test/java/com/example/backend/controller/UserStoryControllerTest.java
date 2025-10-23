package com.example.backend.controller;


import com.example.backend.models.Project;
import com.example.backend.models.UserStory;
import com.example.backend.repo.UserStoryRepo;
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
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserStoryControllerTest
{

    @Mock
    private UserStoryRepo userStoryRepo;

    @InjectMocks
    private UserStoryController userStoryController;

    private UserStory sampleStory;

    @BeforeEach
    void setUp ()
    {
        sampleStory = new UserStory();
        sampleStory.setId(1L);
        sampleStory.setName("Login als Kunde");
        sampleStory.setDescription("Als Kunde möchte ich mich anmelden");
        Project project = new Project();
        project.setId(42L);
        sampleStory.setProject(project);
    }

    @Test
    @DisplayName("getUserStories liefert 200 mit Stories")
    void getUserStoriesReturnsOkWithStories ()
    {
        when(userStoryRepo.findByProjectID(42L)).thenReturn(List.of(sampleStory));

        ResponseEntity<List<UserStory>> response = userStoryController.getUserStories(42L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserStory> stories = Objects.requireNonNull(response.getBody());
        assertThat(stories).containsExactly(sampleStory);
    }

    @Test
    @DisplayName("getUserStories liefert 204 wenn keine Stories existieren")
    void getUserStoriesReturnsNoContentWhenEmpty ()
    {
        when(userStoryRepo.findByProjectID(9L)).thenReturn(List.of());

        ResponseEntity<List<UserStory>> response = userStoryController.getUserStories(9L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("getUserStories liefert 500 bei Fehler")
    void getUserStoriesReturnsInternalServerErrorOnException ()
    {
        when(userStoryRepo.findByProjectID(9L)).thenThrow(new RuntimeException("db down"));

        ResponseEntity<List<UserStory>> response = userStoryController.getUserStories(9L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("getUserStoryById liefert 200 wenn vorhanden")
    void getUserStoryByIdReturnsOkWhenFound ()
    {
        when(userStoryRepo.findById(1L)).thenReturn(Optional.of(sampleStory));

        ResponseEntity<UserStory> response = userStoryController.getUserStoryById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(Objects.requireNonNull(response.getBody())).isEqualTo(sampleStory);
    }

    @Test
    @DisplayName("getUserStoryById liefert 204 wenn Story fehlt")
    void getUserStoryByIdReturnsNoContentWhenMissing ()
    {
        when(userStoryRepo.findById(5L)).thenReturn(Optional.empty());

        ResponseEntity<UserStory> response = userStoryController.getUserStoryById(5L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("getUserStoryById liefert 500 bei Fehler")
    void getUserStoryByIdReturnsInternalServerErrorOnException ()
    {
        when(userStoryRepo.findById(7L)).thenThrow(new RuntimeException("timeout"));

        ResponseEntity<UserStory> response = userStoryController.getUserStoryById(7L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("createUserStory setzt ID auf null und liefert 201")
    void createUserStoryResetsIdAndReturnsCreated ()
    {
        UserStory request = new UserStory();
        request.setId(99L);
        request.setName("Leere Projektliste anzeigen");
        when(userStoryRepo.save(any(UserStory.class))).thenAnswer(invocation ->
        {
            UserStory toPersist = invocation.getArgument(0);
            UserStory persisted = new UserStory();
            persisted.setId(5L);
            persisted.setName(toPersist.getName());
            persisted.setDescription(toPersist.getDescription());
            persisted.setProject(toPersist.getProject());
            return persisted;
        });

        ResponseEntity<UserStory> response = userStoryController.createUserStory(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserStory created = Objects.requireNonNull(response.getBody());
        assertEquals(5L, created.getId());

        ArgumentCaptor<UserStory> captor = ArgumentCaptor.forClass(UserStory.class);
        verify(userStoryRepo).save(captor.capture());
        assertNull(captor.getValue().getId());
    }

    @Test
    @DisplayName("createUserStory liefert 500 bei Fehler")
    void createUserStoryReturnsInternalServerErrorOnException ()
    {
        when(userStoryRepo.save(any(UserStory.class))).thenThrow(new RuntimeException("duplicate"));

        ResponseEntity<UserStory> response = userStoryController.createUserStory(new UserStory());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUserStory überschreibt Inhalte bei vorhandener Story")
    void updateUserStoryReturnsOkWhenExists ()
    {
        when(userStoryRepo.existsById(1L)).thenReturn(true);
        when(userStoryRepo.save(any(UserStory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStory update = new UserStory();
        update.setName("Leerer Projektname erlaubt");
        update.setDescription("");

        ResponseEntity<UserStory> response = userStoryController.updateUserStory(1L, update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserStory persisted = Objects.requireNonNull(response.getBody());
        assertEquals("", persisted.getDescription());
        assertEquals("Leerer Projektname erlaubt", persisted.getName());
    }

    @Test
    @DisplayName("updateUserStory liefert 204 wenn Story fehlt")
    void updateUserStoryReturnsNoContentWhenMissing ()
    {
        when(userStoryRepo.existsById(3L)).thenReturn(false);

        ResponseEntity<UserStory> response = userStoryController.updateUserStory(3L, new UserStory());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUserStory liefert 500 bei Fehler")
    void updateUserStoryReturnsInternalServerErrorOnException ()
    {
        when(userStoryRepo.existsById(2L)).thenThrow(new RuntimeException("constraint"));

        ResponseEntity<UserStory> response = userStoryController.updateUserStory(2L, new UserStory());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("deleteUserStory entfernt Story wenn vorhanden")
    void deleteUserStoryReturnsOkWhenExists ()
    {
        when(userStoryRepo.existsById(1L)).thenReturn(true);
        doNothing().when(userStoryRepo).deleteById(1L);

        ResponseEntity<HttpStatus> response = userStoryController.deleteUserStory(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userStoryRepo).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUserStory liefert 204 wenn Story fehlt")
    void deleteUserStoryReturnsNoContentWhenMissing ()
    {
        when(userStoryRepo.existsById(4L)).thenReturn(false);

        ResponseEntity<HttpStatus> response = userStoryController.deleteUserStory(4L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userStoryRepo).existsById(4L);
    }

    @Test
    @DisplayName("deleteUserStory liefert 500 bei Fehler")
    void deleteUserStoryReturnsInternalServerErrorOnException ()
    {
        when(userStoryRepo.existsById(6L)).thenReturn(true);
        doThrow(new RuntimeException("locked")).when(userStoryRepo).deleteById(6L);

        ResponseEntity<HttpStatus> response = userStoryController.deleteUserStory(6L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
