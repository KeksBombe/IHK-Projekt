package com.example.backend.controller;


import com.example.backend.models.UserStory;
import com.example.backend.repo.UserStoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class UserStoryControllerTest
{

    @Mock
    private UserStoryRepo userStoryRepo;

    @InjectMocks
    private UserStoryController userStoryController;

    @BeforeEach
    void setUp ()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserStories_ReturnsList_WhenUserStoriesExist ()
    {
        // Arrange
        Long projectId = 1L;
        UserStory us1 = new UserStory();
        us1.setId(101L);
        us1.setProjectID(projectId);

        UserStory us2 = new UserStory();
        us2.setId(102L);
        us2.setProjectID(projectId);

        when(userStoryRepo.findByProjectID(projectId)).thenReturn(List.of(us1, us2));

        // Act
        ResponseEntity<List<UserStory>> response = userStoryController.getUserStories(projectId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(userStoryRepo, times(1)).findByProjectID(projectId);
    }


    @Test
    void testGetUserStories_ReturnsNoContent_WhenNoStoriesFound ()
    {
        // Arrange
        Long projectId = 2L;
        when(userStoryRepo.findByProjectID(projectId)).thenReturn(List.of());

        // Act
        ResponseEntity<List<UserStory>> response = userStoryController.getUserStories(projectId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userStoryRepo, times(1)).findByProjectID(projectId);
    }

    @Test
    void testCreateUserStory_ReturnsCreatedUserStory ()
    {
        // Arrange
        UserStory newUserStory = new UserStory();
        newUserStory.setProjectID(1L);

        UserStory savedUserStory = new UserStory();
        savedUserStory.setId(101L);
        savedUserStory.setProjectID(1L);

        when(userStoryRepo.save(newUserStory)).thenReturn(savedUserStory);

        // Act
        ResponseEntity<UserStory> response = userStoryController.createUserStory(newUserStory);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(101L, response.getBody().getId());
        verify(userStoryRepo, times(1)).save(newUserStory);
    }

    @Test
    void testGetUserStories_DeletesUserStory_WhenIdExists ()
    {
        // Arrange
        Long userStoryId = 101L;
        when(userStoryRepo.existsById(userStoryId)).thenReturn(true);
        doNothing().when(userStoryRepo).deleteById(userStoryId);

        // Act
        ResponseEntity<HttpStatus> response = userStoryController.deleteUserStory(userStoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userStoryRepo, times(1)).existsById(userStoryId);
        verify(userStoryRepo, times(1)).deleteById(userStoryId);
    }

    @Test
    void testGetUserStories_ReturnsNotFound_WhenIdDoesNotExist ()
    {
        // Arrange
        Long userStoryId = 999L;
        when(userStoryRepo.existsById(userStoryId)).thenReturn(false);

        // Act
        ResponseEntity<HttpStatus> response = userStoryController.deleteUserStory(userStoryId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userStoryRepo, times(1)).existsById(userStoryId);
        verify(userStoryRepo, never()).deleteById(anyLong());
    }

    @Test
    void testUpdateUserStory_ReturnsUpdatedUserStory_WhenIdExists ()
    {
        // Arrange
        Long userStoryId = 101L;
        UserStory updatedUserStory = new UserStory();
        updatedUserStory.setProjectID(1L);

        UserStory savedUserStory = new UserStory();
        savedUserStory.setId(userStoryId);
        savedUserStory.setProjectID(1L);

        when(userStoryRepo.existsById(userStoryId)).thenReturn(true);
        when(userStoryRepo.save(any(UserStory.class))).thenReturn(savedUserStory);

        // Act
        ResponseEntity<UserStory> response = userStoryController.updateUserStory(userStoryId, updatedUserStory);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userStoryId, response.getBody().getId());
        verify(userStoryRepo, times(1)).existsById(userStoryId);
        verify(userStoryRepo, times(1)).save(any(UserStory.class));
    }

    @Test
    void testUpdateUserStory_ReturnsNoContent_WhenIdDoesNotExist ()
    {
        // Arrange
        Long userStoryId = 999L;
        UserStory updatedUserStory = new UserStory();
        updatedUserStory.setProjectID(1L);

        when(userStoryRepo.existsById(userStoryId)).thenReturn(false);

        // Act
        ResponseEntity<UserStory> response = userStoryController.updateUserStory(userStoryId, updatedUserStory);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userStoryRepo, times(1)).existsById(userStoryId);
        verify(userStoryRepo, never()).save(any(UserStory.class));
    }

    @Test
    void testUpdateUserStory_ReturnsInternalServerError_OnException ()
    {
        // Arrange
        Long userStoryId = 101L;
        UserStory updatedUserStory = new UserStory();
        updatedUserStory.setProjectID(1L);

        when(userStoryRepo.existsById(userStoryId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<UserStory> response = userStoryController.updateUserStory(userStoryId, updatedUserStory);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userStoryRepo, times(1)).existsById(userStoryId);
        verify(userStoryRepo, never()).save(any(UserStory.class));
    }

    @Test
    void testGetUserStories_ReturnsInternalServerError_OnException ()
    {
        // Arrange
        Long projectId = 1L;
        when(userStoryRepo.findByProjectID(projectId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<UserStory>> response = userStoryController.getUserStories(projectId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userStoryRepo, times(1)).findByProjectID(projectId);
    }

    @Test
    void testCreateUserStory_ReturnsInternalServerError_OnException ()
    {
        // Arrange
        UserStory newUserStory = new UserStory();
        newUserStory.setProjectID(1L);

        when(userStoryRepo.save(newUserStory)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<UserStory> response = userStoryController.createUserStory(newUserStory);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userStoryRepo, times(1)).save(newUserStory);
    }

    @Test
    void testDeleteUserStory_ReturnsInternalServerError_OnException ()
    {
        // Arrange
        Long userStoryId = 101L;
        when(userStoryRepo.existsById(userStoryId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<HttpStatus> response = userStoryController.deleteUserStory(userStoryId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userStoryRepo, times(1)).existsById(userStoryId);
    }
}
