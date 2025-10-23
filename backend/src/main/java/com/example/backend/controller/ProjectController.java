package com.example.backend.controller;


import com.example.backend.dto.CreateProjectRequest;
import com.example.backend.dto.ProjectDto;
import com.example.backend.dto.RenameProjectRequest;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.mapper.ProjectMapper;
import com.example.backend.models.Project;
import com.example.backend.repo.ProjectRepo;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
public class ProjectController
{

    private final ProjectRepo projectRepo;
    private final ProjectMapper projectMapper;

    public ProjectController (ProjectRepo projectRepo, ProjectMapper projectMapper)
    {
        this.projectRepo = projectRepo;
        this.projectMapper = projectMapper;
    }

    @GetMapping("/getAllProjects")
    public ResponseEntity<List<ProjectDto>> getAllProjects ()
    {
        List<ProjectDto> projects = projectRepo.findAll().stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());

        if (projects.isEmpty())
        {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/getProjectById/{id}")
    public ResponseEntity<ProjectDto> getProjectById (@PathVariable Long id)
    {
        return projectRepo.findById(id)
                .map(projectMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    @PostMapping("/createProject")
    public ResponseEntity<ProjectDto> createProject (@Valid @RequestBody CreateProjectRequest request)
    {
        Project project = projectMapper.toEntity(request);
        Project savedProject = projectRepo.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectMapper.toDto(savedProject));
    }

    @PatchMapping("/renameProject/{id}")
    public ResponseEntity<ProjectDto> renameProject (@Valid @RequestBody RenameProjectRequest request, @PathVariable Long id)
    {
        return projectRepo.findById(id)
                .map(existing ->
                {
                    existing.setName(request.getName());
                    Project saved = projectRepo.save(existing);
                    return ResponseEntity.ok(projectMapper.toDto(saved));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }


    @DeleteMapping("/deleteProject/{id}")
    public ResponseEntity<Void> deleteProject (@PathVariable Long id)
    {
        if (!projectRepo.existsById(id))
        {
            throw new ResourceNotFoundException("Project", id);
        }
        projectRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
