package com.example.backend.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UserStoryDto
{

    private Long id;

    @NotBlank(message = "User story name is required")
    @Size(min = 1, max = 255, message = "User story name must be between 1 and 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Project ID is required")
    private Long projectID;
}
