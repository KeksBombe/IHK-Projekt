package com.example.backend.mapper;


import com.example.backend.dto.CreateProjectRequest;
import com.example.backend.dto.ProjectDto;
import com.example.backend.models.Project;
import org.springframework.stereotype.Component;


@Component
public class ProjectMapper
{

    public ProjectDto toDto (Project project)
    {
        if (project == null)
        {
            return null;
        }

        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        return dto;
    }

    public Project toEntity (ProjectDto dto)
    {
        if (dto == null)
        {
            return null;
        }

        Project project = new Project();
        project.setId(dto.getId());
        project.setName(dto.getName());
        return project;
    }

    public Project toEntity (CreateProjectRequest request)
    {
        if (request == null)
        {
            return null;
        }

        Project project = new Project();
        project.setName(request.getName());
        return project;
    }
}
