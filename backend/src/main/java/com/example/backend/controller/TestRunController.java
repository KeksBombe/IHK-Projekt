package com.example.backend.controller;


import com.example.backend.models.TestRun;
import com.example.backend.repo.TestRepo;
import com.example.backend.repo.TestRunRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/test")
@Slf4j
public class TestRunController
{
    private final TestRepo testRepo;
    private final TestRunRepo testRunRepo;

    public TestRunController (TestRepo testRepo, TestRunRepo testRunRepo)
    {
        this.testRepo = testRepo;
        this.testRunRepo = testRunRepo;
    }

    @GetMapping("/{id}/runs")
    public ResponseEntity<List<TestRun>> getRunsForTest (@PathVariable Long id)
    {
        try
        {
            if (!testRepo.existsById(id))
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            List<TestRun> runs = testRunRepo.findByTest_IdOrderByExecutedAtDesc(id);
            if (runs.isEmpty())
            {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(runs);
        } catch (Exception e)
        {
            log.error("Failed to load test runs for test {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
