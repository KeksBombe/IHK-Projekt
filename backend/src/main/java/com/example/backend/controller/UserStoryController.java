package com.example.backend.controller;


import com.example.backend.dto.CreateUserStoryRequest;
import com.example.backend.dto.UserStoryDto;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.mapper.UserStoryMapper;
import com.example.backend.models.UserStory;
import com.example.backend.repo.UserStoryRepo;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@Slf4j
public class UserStoryController
{

    private final UserStoryRepo userStoryRepo;
    private final UserStoryMapper userStoryMapper;

    public UserStoryController (UserStoryRepo userStoryRepo, UserStoryMapper userStoryMapper)
    {
        this.userStoryRepo = userStoryRepo;
        this.userStoryMapper = userStoryMapper;
    }

    @GetMapping("/getUserStories/{id}")
    public ResponseEntity<List<UserStoryDto>> getUserStories (@PathVariable Long id)
    {
        log.debug("REST request to get UserStories : {}", id);
        List<UserStoryDto> userStories = userStoryRepo.findByProjectID(id).stream()
                .map(userStoryMapper::toDto)
                .collect(Collectors.toList());
        log.debug("Stories for Project {}", userStories);
        return userStories.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(userStories);
    }

    @GetMapping("/userStory/{id}")
    public ResponseEntity<UserStoryDto> getUserStoryById (@PathVariable Long id)
    {
        log.debug("REST request to get UserStory : {}", id);
        return userStoryRepo.findById(id)
                .map(userStoryMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));
    }

    @PostMapping("/userStory")
    public ResponseEntity<UserStoryDto> createUserStory (@Valid @RequestBody CreateUserStoryRequest request)
    {
        log.debug("REST request to save UserStory : {}", request);
        UserStory userStory = userStoryMapper.toEntity(request);
        userStory.setId(null);
        UserStory savedUserStory = userStoryRepo.save(userStory);
        return ResponseEntity.status(HttpStatus.CREATED).body(userStoryMapper.toDto(savedUserStory));
    }

    @PatchMapping("/userStory/{id}")
    public ResponseEntity<UserStoryDto> updateUserStory (@PathVariable Long id, @Valid @RequestBody UserStoryDto userStoryDto)
    {
        if (!userStoryRepo.existsById(id))
        {
            throw new ResourceNotFoundException("UserStory", id);
        }
        userStoryDto.setId(id);
        UserStory userStory = userStoryMapper.toEntity(userStoryDto);
        UserStory savedUserStory = userStoryRepo.save(userStory);
        return ResponseEntity.ok(userStoryMapper.toDto(savedUserStory));
    }

    @DeleteMapping("/deleteUserStory/{id}")
    public ResponseEntity<Void> deleteUserStory (@PathVariable Long id)
    {
        if (!userStoryRepo.existsById(id))
        {
            throw new ResourceNotFoundException("UserStory", id);
        }
        userStoryRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
