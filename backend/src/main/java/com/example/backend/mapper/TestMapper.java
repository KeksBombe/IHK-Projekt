package com.example.backend.mapper;


import com.example.backend.dto.CreateTestRequest;
import com.example.backend.dto.TestDto;
import com.example.backend.models.Environment;
import com.example.backend.models.TestModel;
import com.example.backend.models.TestStep;
import com.example.backend.models.UserStory;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class TestMapper
{

    public TestDto toDto (TestModel test)
    {
        if (test == null)
        {
            return null;
        }

        TestDto dto = new TestDto();
        dto.setId(test.getId());
        dto.setName(test.getName());
        dto.setDescription(test.getDescription());
        dto.setTestCSV(TestDto.stepsToCsv(test.getSteps()));
        dto.setEnvironmentID(test.getEnvironment() != null ? test.getEnvironment().getId() : null);
        dto.setStoryID(test.getUserStory() != null ? test.getUserStory().getId() : null);
        dto.setGenerationState(test.getGenerationState());
        return dto;
    }

    public TestModel toEntity (TestDto dto)
    {
        if (dto == null)
        {
            return null;
        }

        TestModel test = new TestModel();
        test.setId(dto.getId());
        test.setName(dto.getName());
        test.setDescription(dto.getDescription());
        test.setGenerationState(dto.getGenerationState());

        // Map CSV into TestStep entities and attach
        List<TestStep> steps = TestDto.parseCsvToSteps(dto.getTestCSV());
        for (TestStep s : steps)
        {
            s.setTestModel(test);
        }
        test.getSteps().clear();
        test.getSteps().addAll(steps);

        if (dto.getEnvironmentID() != null)
        {
            Environment environment = new Environment();
            environment.setId(dto.getEnvironmentID());
            test.setEnvironment(environment);
        }

        if (dto.getStoryID() != null)
        {
            UserStory userStory = new UserStory();
            userStory.setId(dto.getStoryID());
            test.setUserStory(userStory);
        }

        return test;
    }

    public TestModel toEntity (CreateTestRequest request)
    {
        if (request == null)
        {
            return null;
        }

        TestModel test = new TestModel();
        test.setName(request.getName());
        test.setDescription(request.getDescription());

        // Map CSV into TestStep entities and attach
        List<TestStep> steps = TestDto.parseCsvToSteps(request.getTestCSV());
        for (TestStep s : steps)
        {
            s.setTestModel(test);
        }
        test.getSteps().addAll(steps);

        if (request.getEnvironmentID() != null)
        {
            Environment environment = new Environment();
            environment.setId(request.getEnvironmentID());
            test.setEnvironment(environment);
        }

        if (request.getStoryID() != null)
        {
            UserStory userStory = new UserStory();
            userStory.setId(request.getStoryID());
            test.setUserStory(userStory);
        }

        return test;
    }
}
