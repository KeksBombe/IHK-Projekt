package com.example.backend.service;


import com.example.backend.constants.TestStatus;
import com.example.backend.models.TestRun;
import com.example.backend.models.TestModel;
import com.example.backend.repo.TestRepo;
import com.example.backend.repo.TestRunRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
public class PlaywrightTestRunner
{

    private static final int TIMEOUT_MINUTES = 10;

    private final TestRepo testRepository;
    private final TestRunRepo testRunRepository;

    public PlaywrightTestRunner (TestRepo testRepository, TestRunRepo testRunRepository)
    {
        this.testRepository = testRepository;
        this.testRunRepository = testRunRepository;
    }

    public TestRun runPlaywrightTest (Long testId, String testFileName)
    {
        TestModel testModel = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test with ID " + testId + " not found"));

        TestRun testRun = new TestRun();
        testRun.setTest(testModel);
        testRun.setStatus(TestStatus.PENDING);
        testRun.setExecutedAt(LocalDateTime.now());
        testRun.setDescription("Test execution in progress...");
        testRun = testRunRepository.save(testRun);

        log.info("Created TestRun with ID {} and PENDING status", testRun.getId());

        StringBuilder output = new StringBuilder();

        try
        {
            List<String> command = buildCommand(testFileName);

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            File workingDir = new File(System.getProperty("user.dir"));
            processBuilder.directory(workingDir);

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);

            if (!finished)
            {
                process.destroyForcibly();
                testRun.setStatus(TestStatus.FAILED);
                testRun.setDescription("Test execution timeout after " + TIMEOUT_MINUTES + " minutes\n" + output.toString());
                return testRunRepository.save(testRun);
            }

            int exitCode = process.exitValue();
            String fullOutput = output.toString();

            parseTestResults(testRun, fullOutput, exitCode);

        } catch (Exception e)
        {
            log.error("Error executing Playwright test: {}", testFileName, e);
            testRun.setStatus(TestStatus.FAILED);
            testRun.setDescription("Error: " + e.getMessage());
        }

        return testRunRepository.save(testRun);
    }


    private List<String> buildCommand (String testFileName)
    {
        List<String> command = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        String playwrightCommand = "npx playwright test " + testFileName + " --reporter=json";

        if (os.contains("win"))
        {
            command.add("cmd.exe");
            command.add("/c");
            command.add("cd backend && " + playwrightCommand);
        } else
        {
            command.add("/bin/sh");
            command.add("-c");
            command.add("cd backend && " + playwrightCommand);
        }

        return command;
    }

    private void parseTestResults (TestRun testRun, String output, int exitCode)
    {
        try
        {
            Pattern jsonPattern = Pattern.compile("\\{[\\s\\S]*\"stats\"[\\s\\S]*}");
            Matcher matcher = jsonPattern.matcher(output);

            if (matcher.find())
            {
                String jsonString = matcher.group();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(jsonString);

                JsonNode stats = root.path("stats");
                int unexpected = stats.path("unexpected").asInt(0);
                int expected = stats.path("expected").asInt(0);
                int skipped = stats.path("skipped").asInt(0);
                int flaky = stats.path("flaky").asInt(0);

                log.info("Test results - Expected: {}, Unexpected: {}, Skipped: {}, Flaky: {}",
                        expected, unexpected, skipped, flaky);

                if (unexpected > 0)
                {
                    testRun.setStatus(TestStatus.FAILED);
                    testRun.setDescription(String.format(
                            "Test failed: %d test(s) failed, %d passed, %d skipped",
                            unexpected, expected, skipped
                    ));
                } else if (expected > 0 || exitCode == 0)
                {
                    testRun.setStatus(TestStatus.PASSED);
                    testRun.setDescription(String.format(
                            "All tests passed: %d test(s) passed, %d skipped",
                            expected, skipped
                    ));
                } else
                {
                    testRun.setStatus(TestStatus.SKIPPED);
                    testRun.setDescription("No tests were executed");
                }
            }
        } catch (Exception e)
        {
            log.error("Error parsing test results JSON", e);
            if (exitCode == 0)
            {
                testRun.setStatus(TestStatus.PASSED);
                testRun.setDescription("Test executed successfully (exit code 0)");
            } else
            {
                testRun.setStatus(TestStatus.FAILED);
                testRun.setDescription("Test failed with exit code: " + exitCode);
            }
        }
    }
}
