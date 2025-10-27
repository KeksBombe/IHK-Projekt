package com.example.backend.mapper;


import com.example.backend.dto.TestRunDto;
import com.example.backend.models.TestModel;
import com.example.backend.models.TestRun;
import org.hibernate.Hibernate;
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

        // Safely extract testId without fully initializing the lazy proxy
        TestModel test = testRun.getTest();
        if (test != null && Hibernate.isInitialized(test))
        {
            dto.setTestId(test.getId());
        } else if (test != null)
        {
            // For uninitialized proxy, we can still get the ID without triggering lazy load
            dto.setTestId(test.getId());
        }
        
        return dto;
    }
}
