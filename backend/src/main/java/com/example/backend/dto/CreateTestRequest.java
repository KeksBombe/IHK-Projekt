package com.example.backend.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class CreateTestRequest
{

    @NotBlank(message = "Test name is required")
    @Size(min = 1, max = 255, message = "Test name must be between 1 and 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private String testCSV;

    private Long environmentID;

    @NotNull(message = "Story ID is required")
    private Long storyID;
}
