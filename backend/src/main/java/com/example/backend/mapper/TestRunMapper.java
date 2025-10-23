package com.example.backend.mapper;


import com.example.backend.dto.TestRunDto;
import com.example.backend.models.TestRun;
import org.springframework.stereotype.Component;


@Component
public class TestRunMapper
{

    public TestRunDto toDto (TestRun testRun)
    {
        if (testRun == null)
        {
            return null;
        }

        TestRunDto dto = new TestRunDto();
        dto.setId(testRun.getId());
        dto.setStatus(testRun.getStatus());
        dto.setDescription(testRun.getDescription());
        dto.setExecutedAt(testRun.getExecutedAt());
        dto.setTestId(testRun.getTest() != null ? testRun.getTest().getId() : null);
        return dto;
    }
}
