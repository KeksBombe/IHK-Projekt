package com.example.backend.controller;


import com.example.backend.models.Environment;
import com.example.backend.repo.EnvironmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
public class EnvironmentController
{

    @Autowired
    private EnvironmentRepo environmentRepo;

    private static final String MASKED_PASSWORD = "************";

    /**
     * CRUD operations for Environment entity.
     */

    /**
     * Masks the password in an Environment object
     */
    private Environment maskPassword (Environment env)
    {
        Environment maskedEnv = new Environment();
        maskedEnv.setId(env.getId());
        maskedEnv.setName(env.getName());
        maskedEnv.setUrl(env.getUrl());
        maskedEnv.setUsername(env.getUsername());
        maskedEnv.setPassword(MASKED_PASSWORD);
        maskedEnv.setProjectID(env.getProjectID());
        return maskedEnv;
    }

    @GetMapping("/getEnvironmentById/{id}")
    public ResponseEntity<Environment> getEnvironmentById (@PathVariable Long id)
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
    public ResponseEntity<Environment> createEnvironment (@RequestBody Environment environment)
    {
        try
        {
            environment.setId(null);
            Environment environmentObj = environmentRepo.save(environment);
            return ResponseEntity.status(HttpStatus.CREATED).body(maskPassword(environmentObj));
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updateEnvironment/{id}")
    public ResponseEntity<Environment> updateEnvironment (@RequestBody Environment environment, @PathVariable Long id)
    {
        try
        {
            Optional<Environment> oldEnvironmentObj = environmentRepo.findById(id);

            if (oldEnvironmentObj.isPresent())
            {
                Environment updatedEnvironmentData = oldEnvironmentObj.get();
                updatedEnvironmentData.setName(environment.getName());
                updatedEnvironmentData.setUrl(environment.getUrl());
                updatedEnvironmentData.setUsername(environment.getUsername());

                if (!MASKED_PASSWORD.equals(environment.getPassword()))
                {
                    updatedEnvironmentData.setPassword(environment.getPassword());
                }

                Environment environmentObj = environmentRepo.save(updatedEnvironmentData);
                return ResponseEntity.ok(maskPassword(environmentObj));
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
    public ResponseEntity<List<Environment>> getEnvironmentsByProjectId (@PathVariable Long projectId)
    {
        try
        {
            List<Environment> environments = environmentRepo.findByProjectId(projectId);
            if (environments.isEmpty())
            {
                return ResponseEntity.noContent().build();
            }

            List<Environment> maskedEnvironments = environments.stream()
                    .map(this::maskPassword)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(maskedEnvironments);
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}