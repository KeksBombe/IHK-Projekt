package com.example.backend.controller;


import com.example.backend.models.UserStory;
import com.example.backend.repo.UserStoryRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
public class UserStoryController
{

    private final UserStoryRepo userStoryRepo;

    public UserStoryController (UserStoryRepo userStoryRepo)
    {
        this.userStoryRepo = userStoryRepo;
    }

    @GetMapping("/getUserStories/{id}")
    public ResponseEntity<List<UserStory>> getUserStories (@PathVariable Long id)
    {
        log.debug("REST request to get UserStories : {}", id);
        try
        {
            List<UserStory> userStories = userStoryRepo.findByProjectID(id);
            log.debug("Storys for Project {}", userStories.toString());
            return userStories.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(userStories);
        } catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/userStory")
    public ResponseEntity<UserStory> createUserStory (@RequestBody UserStory userStory)
    {
        log.debug("REST request to save UserStory : {}", userStory);
        try
        {
            userStory.setId(null);
            UserStory userStoryObj = userStoryRepo.save(userStory);
            return ResponseEntity.status(HttpStatus.CREATED).body(userStoryObj);
        } catch (Exception e)
        {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/userStory/{id}")
    public ResponseEntity<UserStory> updateUserStory (@PathVariable Long id, @RequestBody UserStory userStory)
    {
        try
        {
            if (userStoryRepo.existsById(id))
            {
                userStory.setId(id);
                UserStory userStoryObj = userStoryRepo.save(userStory);
                return ResponseEntity.ok(userStoryObj);
            } else
            {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e)
        {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/deleteUserStory/{id}")
    public ResponseEntity<HttpStatus> deleteUserStory (@PathVariable Long id)
    {
        try
        {
            if (userStoryRepo.existsById(id))
            {
                userStoryRepo.deleteById(id);
                return ResponseEntity.ok().build();
            } else
            {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

}
