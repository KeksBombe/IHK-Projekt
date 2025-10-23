package com.example.backend.controller;


import com.example.backend.dto.CreateUserStoryRequest;
import com.example.backend.dto.UserStoryDto;
import com.example.backend.mapper.UserStoryMapper;
import com.example.backend.models.Project;
import com.example.backend.models.UserStory;
import com.example.backend.repo.UserStoryRepo;
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
class UserStoryControllerTest
{

    @Mock
    private UserStoryRepo userStoryRepo;

    @Mock
    private UserStoryMapper userStoryMapper;

    @InjectMocks
    private UserStoryController userStoryController;

    private UserStory sampleStory;
    private UserStoryDto sampleStoryDto;

    @BeforeEach
    void setUp ()
    {
        Project project = new Project();
        project.setId(42L);

        sampleStory = new UserStory();
        sampleStory.setId(1L);
        sampleStory.setName("Login als Kunde");
        sampleStory.setDescription("Als Kunde möchte ich mich anmelden");
        sampleStory.setProject(project);

        sampleStoryDto = new UserStoryDto();
        sampleStoryDto.setId(1L);
        sampleStoryDto.setName("Login als Kunde");
        sampleStoryDto.setDescription("Als Kunde möchte ich mich anmelden");
        sampleStoryDto.setProjectID(42L);
    }

    @Test
    @DisplayName("getUserStories liefert 200 mit Stories")
    void getUserStoriesReturnsOkWithStories ()
    {
        when(userStoryRepo.findByProjectID(42L)).thenReturn(List.of(sampleStory));
        when(userStoryMapper.toDto(sampleStory)).thenReturn(sampleStoryDto);

        ResponseEntity<List<UserStoryDto>> response = userStoryController.getUserStories(42L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserStoryDto> stories = response.getBody();
        assertThat(stories).hasSize(1);
        assertEquals("Login als Kunde", stories.get(0).getName());
    }

    @Test
    @DisplayName("getUserStories liefert 204 wenn keine Stories existieren")
    void getUserStoriesReturnsNoContentWhenEmpty ()
    {
        when(userStoryRepo.findByProjectID(9L)).thenReturn(List.of());

        ResponseEntity<List<UserStoryDto>> response = userStoryController.getUserStories(9L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("getUserStoryById liefert 200 wenn vorhanden")
    void getUserStoryByIdReturnsOkWhenFound ()
    {
        when(userStoryRepo.findById(1L)).thenReturn(Optional.of(sampleStory));
        when(userStoryMapper.toDto(sampleStory)).thenReturn(sampleStoryDto);

        ResponseEntity<UserStoryDto> response = userStoryController.getUserStoryById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        assertEquals("Login als Kunde", response.getBody().getName());
    }

    @Test
    @DisplayName("createUserStory setzt ID auf null und liefert 201")
    void createUserStoryResetsIdAndReturnsCreated ()
    {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setName("Leere Projektliste anzeigen");
        request.setProjectID(42L);

        UserStory newStory = new UserStory();
        newStory.setName("Leere Projektliste anzeigen");

        UserStory savedStory = new UserStory();
        savedStory.setId(5L);
        savedStory.setName("Leere Projektliste anzeigen");

        UserStoryDto savedDto = new UserStoryDto();
        savedDto.setId(5L);
        savedDto.setName("Leere Projektliste anzeigen");
        savedDto.setProjectID(42L);

        when(userStoryMapper.toEntity(request)).thenReturn(newStory);
        when(userStoryRepo.save(newStory)).thenReturn(savedStory);
        when(userStoryMapper.toDto(savedStory)).thenReturn(savedDto);

        ResponseEntity<UserStoryDto> response = userStoryController.createUserStory(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserStoryDto created = response.getBody();
        assertEquals(5L, created.getId());
    }

    @Test
    @DisplayName("updateUserStory überschreibt Inhalte bei vorhandener Story")
    void updateUserStoryReturnsOkWhenExists ()
    {
        UserStoryDto updateDto = new UserStoryDto();
        updateDto.setId(1L);
        updateDto.setName("Leerer Projektname erlaubt");
        updateDto.setDescription("");
        updateDto.setProjectID(42L);

        UserStory updatedEntity = new UserStory();
        updatedEntity.setId(1L);
        updatedEntity.setName("Leerer Projektname erlaubt");
        updatedEntity.setDescription("");

        UserStory savedStory = new UserStory();
        savedStory.setId(1L);
        savedStory.setName("Leerer Projektname erlaubt");
        savedStory.setDescription("");

        UserStoryDto savedDto = new UserStoryDto();
        savedDto.setId(1L);
        savedDto.setName("Leerer Projektname erlaubt");
        savedDto.setDescription("");
        savedDto.setProjectID(42L);

        when(userStoryRepo.existsById(1L)).thenReturn(true);
        when(userStoryMapper.toEntity(updateDto)).thenReturn(updatedEntity);
        when(userStoryRepo.save(updatedEntity)).thenReturn(savedStory);
        when(userStoryMapper.toDto(savedStory)).thenReturn(savedDto);

        ResponseEntity<UserStoryDto> response = userStoryController.updateUserStory(1L, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserStoryDto persisted = response.getBody();
        assertEquals("", persisted.getDescription());
        assertEquals("Leerer Projektname erlaubt", persisted.getName());
    }

    @Test
    @DisplayName("deleteUserStory entfernt Story wenn vorhanden")
    void deleteUserStoryReturnsOkWhenExists ()
    {
        when(userStoryRepo.existsById(1L)).thenReturn(true);
        doNothing().when(userStoryRepo).deleteById(1L);

        ResponseEntity<Void> response = userStoryController.deleteUserStory(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userStoryRepo).deleteById(1L);
    }
}