package com.example.backend.mapper;


import com.example.backend.dto.CreateUserStoryRequest;
import com.example.backend.dto.UserStoryDto;
import com.example.backend.models.Project;
import com.example.backend.models.UserStory;
import org.springframework.stereotype.Component;


@Component
public class UserStoryMapper
{

    public UserStoryDto toDto (UserStory userStory)
    {
        if (userStory == null)
        {
            return null;
        }

        UserStoryDto dto = new UserStoryDto();
        dto.setId(userStory.getId());
        dto.setName(userStory.getName());
        dto.setDescription(userStory.getDescription());
        dto.setProjectID(userStory.getProject() != null ? userStory.getProject().getId() : null);
        return dto;
    }

    public UserStory toEntity (UserStoryDto dto)
    {
        if (dto == null)
        {
            return null;
        }

        UserStory userStory = new UserStory();
        userStory.setId(dto.getId());
        userStory.setName(dto.getName());
        userStory.setDescription(dto.getDescription());

        if (dto.getProjectID() != null)
        {
            Project project = new Project();
            project.setId(dto.getProjectID());
            userStory.setProject(project);
        }

        return userStory;
    }

    public UserStory toEntity (CreateUserStoryRequest request)
    {
        if (request == null)
        {
            return null;
        }

        UserStory userStory = new UserStory();
        userStory.setName(request.getName());
        userStory.setDescription(request.getDescription());

        if (request.getProjectID() != null)
        {
            Project project = new Project();
            project.setId(request.getProjectID());
            userStory.setProject(project);
        }

        return userStory;
    }
}
