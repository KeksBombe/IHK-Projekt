package com.example.backend.controller;


import com.example.backend.constants.EFileType;
import com.example.backend.dto.CreateTestRequest;
import com.example.backend.dto.TestDto;
import com.example.backend.dto.TestRunDto;
import com.example.backend.exceptions.FileOperationException;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.mapper.TestMapper;
import com.example.backend.mapper.TestRunMapper;
import com.example.backend.models.PlaywrightTest;
import com.example.backend.models.TestModel;
import com.example.backend.models.TestRun;
import com.example.backend.repo.TestRepo;
import com.example.backend.service.AiService;
import com.example.backend.service.FileService;
import com.example.backend.service.PlaywrightTestRunner;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@Slf4j
public class TestController
{

    private final TestRepo testRepo;
    private final TestMapper testMapper;
    private final TestRunMapper testRunMapper;
    private final AiService aiService;
    private final FileService fileService;
    private final PlaywrightTestRunner playwrightTestRunner;

    public TestController (TestRepo testRepo, TestMapper testMapper, TestRunMapper testRunMapper, AiService aiService, FileService fileservice, PlaywrightTestRunner playwrightTestRunner)
    {
        this.testRepo = testRepo;
        this.testMapper = testMapper;
        this.testRunMapper = testRunMapper;
        this.aiService = aiService;
        this.fileService = fileservice;
        this.playwrightTestRunner = playwrightTestRunner;
    }

    @GetMapping("/getTests/{id}")
    public ResponseEntity<List<TestDto>> getTests (@PathVariable Long id)
    {
        List<TestDto> tests = testRepo.findByStoryID(id).stream()
                .map(testMapper::toDto)
                .collect(Collectors.toList());
        return tests.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(tests);
    }

    @GetMapping("/test/{id}")
    public ResponseEntity<TestDto> getTestById (@PathVariable Long id)
    {
        return testRepo.findById(id)
                .map(testMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Test", id));
    }

    @PostMapping("/test")
    public ResponseEntity<TestDto> createTest (@Valid @RequestBody CreateTestRequest request)
    {
        TestModel test = testMapper.toEntity(request);
        test.setId(null);
        TestModel savedTest = testRepo.save(test);
        return ResponseEntity.status(HttpStatus.CREATED).body(testMapper.toDto(savedTest));
    }

    @PatchMapping("/test/{id}")
    public ResponseEntity<TestDto> updateTest (@PathVariable Long id, @Valid @RequestBody TestDto testDto)
    {
        if (!testRepo.existsById(id))
        {
            throw new ResourceNotFoundException("Test", id);
        }
        testDto.setId(id);
        TestModel test = testMapper.toEntity(testDto);
        TestModel savedTest = testRepo.save(test);
        return ResponseEntity.ok(testMapper.toDto(savedTest));
    }

    @DeleteMapping("/test/{id}")
    public ResponseEntity<Void> deleteTest (@PathVariable Long id)
    {
        if (!testRepo.existsById(id))
        {
            throw new ResourceNotFoundException("Test", id);
        }
        testRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/{id}")
    public ResponseEntity<String> generateTest (@PathVariable Long id)
    {
        TestModel test = testRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test", id));

        log.info("Generating Playwright tests for test ID: {}", id);
        String result = aiService.generateAndRunTests(test);

        try
        {
            fileService.writeFile(test.getId().toString(), result, EFileType.SPEC_TS);
        } catch (IOException e)
        {
            throw new FileOperationException("Failed to write test file for test ID: " + id, e);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/test/code/{id}")
    public ResponseEntity<PlaywrightTest> getTestCodeById (@PathVariable Long id)
    {
        PlaywrightTest playwrightTest = new PlaywrightTest();
        String fileName = id.toString() + EFileType.SPEC_TS.getExtension();
        String code;

        try
        {
            code = fileService.readFile(fileName);
        } catch (IOException e)
        {
            log.warn("No test code file found for ID: {}", id);
            return ResponseEntity.noContent().build();
        }

        playwrightTest.setTestID(id);
        playwrightTest.setCode(code);

        return ResponseEntity.ok(playwrightTest);
    }

    @PutMapping("/test/code/{id}")
    public ResponseEntity<String> saveTestCodeById (@PathVariable Long id, @RequestBody PlaywrightTest playwrightTest)
    {
        if (!testRepo.existsById(id))
        {
            throw new ResourceNotFoundException("Test", id);
        }

        try
        {
            fileService.writeFile(id.toString(), playwrightTest.getCode(), EFileType.SPEC_TS);
        } catch (IOException e)
        {
            throw new FileOperationException("Failed to save test code for test ID: " + id, e);
        }

        log.info("Successfully saved test code for ID: {}", id);
        return ResponseEntity.ok("Test code saved successfully");
    }

    @PostMapping("/test/execute/{id}")
    public ResponseEntity<TestRunDto> executePlaywrightTest (@PathVariable Long id)
    {
        if (!testRepo.existsById(id))
        {
            throw new ResourceNotFoundException("Test", id);
        }

        String testFileName = id + ".spec.ts";
        log.info("Executing Playwright test: {}", testFileName);

        TestRun testRun = playwrightTestRunner.runPlaywrightTest(id, testFileName);

        return ResponseEntity.ok(testRunMapper.toDto(testRun));
    }
}
