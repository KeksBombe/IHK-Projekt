package com.example.backend.dto;


import com.example.backend.constants.TestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class TestRunDto
{

    private Long id;

    private TestStatus status;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime executedAt;

    private Long testId;
}
