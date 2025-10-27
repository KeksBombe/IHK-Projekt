package com.example.backend.controller;


import com.example.backend.dto.EnvironmentDto;
import com.example.backend.mapper.EnvironmentMapper;
import com.example.backend.models.Environment;
import com.example.backend.models.Project;
import com.example.backend.repo.EnvironmentRepo;
import com.example.backend.repo.ProjectRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@RestController
public class EnvironmentController
{

    private final EnvironmentRepo environmentRepo;
    private final EnvironmentMapper environmentMapper;
    private final ProjectRepo projectRepo;

    private static final String MASKED_PASSWORD = "************";

    public EnvironmentController (EnvironmentRepo environmentRepo, EnvironmentMapper environmentMapper, ProjectRepo projectRepo)
    {
        this.environmentRepo = environmentRepo;
        this.environmentMapper = environmentMapper;
        this.projectRepo = projectRepo;
    }

    /**
     * Masks the password in an EnvironmentDto object
     */
    private EnvironmentDto maskPassword (Environment env)
    {
        EnvironmentDto dto = environmentMapper.toDto(env);
        if (dto != null)
        {
            dto.setPassword(MASKED_PASSWORD);
        }
        return dto;
    }

    @GetMapping("/getEnvironmentById/{id}")
    public ResponseEntity<EnvironmentDto> getEnvironmentById (@PathVariable Long id)
    {
        try
        {
            Optional<Environment> environment = environmentRepo.findById(id);
            return environment
                    .map(this::maskPassword)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/createEnvironment")
    public ResponseEntity<EnvironmentDto> createEnvironment (@RequestBody EnvironmentDto environmentDto)
    {
        try
        {
            Environment environment = environmentMapper.toEntity(environmentDto);
            environment.setId(null);

            // Fetch and set the managed Project entity if projectID is provided
            if (environmentDto.getProjectID() != null)
            {
                Optional<Project> project = projectRepo.findById(environmentDto.getProjectID());
                if (project.isPresent())
                {
                    environment.setProject(project.get());
                } else
                {
                    log.error("Project with ID {} not found", environmentDto.getProjectID());
                    return ResponseEntity.badRequest().build();
                }
            }
            
            Environment savedEnvironment = environmentRepo.save(environment);
            return ResponseEntity.status(HttpStatus.CREATED).body(maskPassword(savedEnvironment));
        } catch (Exception e)
        {
            log.error("Error creating environment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateEnvironment/{id}")
    public ResponseEntity<EnvironmentDto> updateEnvironment (@RequestBody EnvironmentDto environmentDto, @PathVariable Long id)
    {
        try
        {
            Optional<Environment> oldEnvironmentObj = environmentRepo.findById(id);

            if (oldEnvironmentObj.isPresent())
            {
                Environment updatedEnvironmentData = oldEnvironmentObj.get();
                updatedEnvironmentData.setName(environmentDto.getName());
                updatedEnvironmentData.setUrl(environmentDto.getUrl());
                updatedEnvironmentData.setUsername(environmentDto.getUsername());

                if (!MASKED_PASSWORD.equals(environmentDto.getPassword()))
                {
                    updatedEnvironmentData.setPassword(environmentDto.getPassword());
                }

                Environment savedEnvironment = environmentRepo.save(updatedEnvironmentData);
                return ResponseEntity.ok(maskPassword(savedEnvironment));
            } else
            {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteEnvironment/{id}")
    public ResponseEntity<Void> deleteEnvironment (@PathVariable Long id)
    {
        try
        {
            environmentRepo.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getEnvironmentsByProjectId/{projectId}")
    public ResponseEntity<List<EnvironmentDto>> getEnvironmentsByProjectId (@PathVariable Long projectId)
    {
        try
        {
            List<Environment> environments = environmentRepo.findByProjectId(projectId);

            if (environments.isEmpty())
            {
                return ResponseEntity.noContent().build();
            }

            log.debug("Found {} environments for projectId {}", environments.size(), projectId);

            List<EnvironmentDto> maskedEnvironments = environments.stream()
                    .map(this::maskPassword)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(maskedEnvironments);
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}