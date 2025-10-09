package com.example.backend.controller;


import com.example.backend.models.Project;
import com.example.backend.repo.ProjectRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
public class ProjectController
{

    @Autowired
    private ProjectRepo projectRepo;

    @GetMapping("/getAllProjects")
    public ResponseEntity<List<Project>> getAllProjects ()
    {
        try
        {
            List<Project> projects = new ArrayList<>(projectRepo.findAll());

            if (projects.isEmpty())
            {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(projects);
        } catch (Exception ex)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getProjectById/{id}")
    public ResponseEntity<Project> getProjectById (@PathVariable Long id)
    {
        try
        {
            Optional<Project> project = projectRepo.findById(id);
            if (project.isPresent())
            {
                return ResponseEntity.ok(project.get());
            } else
            {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/createProject")
    public ResponseEntity<Project> createProject (@RequestBody Project project)
    {
        try
        {
            Project projectObj = projectRepo.save(project);
            return ResponseEntity.status(HttpStatus.CREATED).body(projectObj);
        } catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/renameProject/{id}")
    public ResponseEntity<Project> updateProject (@RequestBody Project project, @PathVariable Long id)
    {
        try
        {
            return projectRepo.findById(id)
                    .map(existing ->
                    {
                        existing.setName(project.getName());
                        Project saved = projectRepo.save(existing);
                        return ResponseEntity.ok(saved);
                    })
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }


    @DeleteMapping("/deleteProject/{id}")
    public ResponseEntity<HttpStatus> deleteProject (@PathVariable Long id)
    {
        try
        {
            projectRepo.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }
}
