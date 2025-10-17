package com.example.backend.controller;


import com.example.backend.constants.EFileType;
import com.example.backend.models.PlaywrightTest;
import com.example.backend.models.TestModel;
import com.example.backend.models.TestRun;
import com.example.backend.repo.TestRepo;
import com.example.backend.service.AiService;
import com.example.backend.service.FileService;
import com.example.backend.service.PlaywrightTestRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
@Slf4j
public class TestController
{

    private final TestRepo testRepo;
    private final AiService aiService;
    private final FileService fileService;
    private final PlaywrightTestRunner playwrightTestRunner;

    public TestController (TestRepo testRepo, AiService aiService, FileService fileservice, PlaywrightTestRunner playwrightTestRunner)
    {
        this.testRepo = testRepo;
        this.aiService = aiService;
        this.fileService = fileservice;
        this.playwrightTestRunner = playwrightTestRunner;
    }

    @GetMapping("/getTests/{id}")
    public ResponseEntity<List<TestModel>> getTests (@PathVariable Long id)
    {
        try
        {
            List<TestModel> tests = testRepo.findByStoryID(id);
            return tests.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(tests);
        } catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/test/{id}")
    public ResponseEntity<TestModel> getTestById (@PathVariable Long id)
    {
        try
        {
            return testRepo.findById(id)
                    .map(test -> ResponseEntity.ok().body(test))
                    .orElseGet(() -> ResponseEntity.noContent().build());
        } catch (Exception e)
        {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/test")
    public ResponseEntity<TestModel> createTest (@RequestBody TestModel test)
    {
        try
        {
            test.setId(null);
            TestModel testObj = testRepo.save(test);
            return ResponseEntity.status(HttpStatus.CREATED).body(testObj);
        } catch (Exception e)
        {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/test/{id}")
    public ResponseEntity<TestModel> updateTest (@PathVariable Long id, @RequestBody TestModel test)
    {
        try
        {
            if (testRepo.existsById(id))
            {
                test.setId(id);
                TestModel testObj = testRepo.save(test);
                return ResponseEntity.ok(testObj);
            } else
            {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e)
        {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/test/{id}")
    public ResponseEntity<HttpStatus> deleteTest (@PathVariable Long id)
    {
        try
        {
            if (testRepo.existsById(id))
            {
                testRepo.deleteById(id);
                return ResponseEntity.ok().build();
            } else
            {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate/{id}")
    public ResponseEntity<String> generateTest (@PathVariable Long id)
    {
        try
        {
            // Get the test by ID
            return testRepo.findById(id)
                    .map(test ->
                    {
                        try
                        {
                            log.info("Generating Playwright tests for test ID: {}", id);
                            // Call AI service to generate and run tests
                            String result = aiService.generateAndRunTests(test);
                            this.fileService.writeFile(test.getId().toString(), result, EFileType.SPEC_TS);
                            return ResponseEntity.ok(result);
                        } catch (Exception e)
                        {
                            log.error("Error generating tests for ID: {}", id, e);
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body("Error generating tests: " + e.getMessage());
                        }
                    })
                    .orElseGet(() ->
                    {
                        log.warn("Test with ID {} not found", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Test with ID " + id + " not found");
                    });
        } catch (Exception e)
        {
            log.error("Error in generate endpoint for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/test/code/{id}")
    public ResponseEntity<PlaywrightTest> getTestCodeById (@PathVariable Long id)
    {
        try
        {
            PlaywrightTest playwrightTest = new PlaywrightTest();
            String fileName = id.toString() + EFileType.SPEC_TS.getExtension();
            String code;
            try
            {
                code = this.fileService.readFile(fileName);
            } catch (IOException e)
            {
                log.warn("No test code file found for ID: {}", id);
                return ResponseEntity.noContent().build();
            }

            playwrightTest.setTestID(id);
            playwrightTest.setCode(code);

            return ResponseEntity.ok(playwrightTest);
        } catch (Exception e)
        {
            log.error("Error reading test code for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/test/code/{id}")
    public ResponseEntity<String> saveTestCodeById (@PathVariable Long id, @RequestBody PlaywrightTest playwrightTest)
    {
        try
        {
            if (!testRepo.existsById(id))
            {
                log.warn("Test with ID {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Test with ID " + id + " not found");
            }

            this.fileService.writeFile(id.toString(), playwrightTest.getCode(), EFileType.SPEC_TS);
            log.info("Successfully saved test code for ID: {}", id);
            return ResponseEntity.ok("Test code saved successfully");
        } catch (IOException e)
        {
            log.error("Error saving test code for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving test code: " + e.getMessage());
        } catch (Exception e)
        {
            log.error("Error in save test code endpoint for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/test/execute/{id}")
    public ResponseEntity<TestRun> executePlaywrightTest (@PathVariable Long id)
    {
        try
        {
            // Check if test exists
            if (!testRepo.existsById(id))
            {
                log.warn("Test with ID {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String testFileName = id.toString() + ".spec.ts";
            log.info("Executing Playwright test: {}", testFileName);

            TestRun testRun = playwrightTestRunner.runPlaywrightTest(id, testFileName);

            return ResponseEntity.ok(testRun);

        } catch (Exception e)
        {
            log.error("Error in execute test endpoint for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
