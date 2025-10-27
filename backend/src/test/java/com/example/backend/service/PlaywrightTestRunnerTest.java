package com.example.backend.service;


import com.example.backend.constants.TestStatus;
import com.example.backend.models.TestRun;
import com.example.backend.repo.TestRepo;
import com.example.backend.repo.TestRunRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class PlaywrightTestRunnerTest
{

    private PlaywrightTestRunner runner;
    private Method parseMethod;
    private Method buildCommandMethod;
    private String originalOsName;

    @Mock
    private TestRepo testRepository;

    @Mock
    private TestRunRepo testRunRepository;

    @BeforeEach
    void setUp () throws Exception
    {
        runner = new PlaywrightTestRunner(testRepository, testRunRepository);
        parseMethod = PlaywrightTestRunner.class.getDeclaredMethod("parseTestResults", TestRun.class, String.class, int.class);
        parseMethod.setAccessible(true);
        buildCommandMethod = PlaywrightTestRunner.class.getDeclaredMethod("buildCommand", String.class);
        buildCommandMethod.setAccessible(true);
        originalOsName = System.getProperty("os.name");
    }

    @AfterEach
    void tearDown ()
    {
        if (originalOsName != null)
        {
            System.setProperty("os.name", originalOsName);
        }
    }

    @Test
    @DisplayName("parseTestResults setzt Status FAILED bei unerwarteten Tests")
    void parseTestResultsMarksFailedWhenUnexpectedPresent () throws Exception
    {
        String json = "{\"stats\":{\"unexpected\":1,\"expected\":2,\"skipped\":0,\"flaky\":0}}";
        TestRun run = new TestRun();

        invokeParse(run, json, 1);

        assertEquals(TestStatus.FAILED, run.getStatus());
        assertThat(run.getDescription()).contains("failed");
    }

    @Test
    @DisplayName("parseTestResults setzt Status PASSED bei erfolgreichen Tests")
    void parseTestResultsMarksPassedWhenExpectedPresent () throws Exception
    {
        String json = "{\"stats\":{\"unexpected\":0,\"expected\":3,\"skipped\":1,\"flaky\":0}}";
        TestRun run = new TestRun();

        invokeParse(run, json, 0);

        assertEquals(TestStatus.PASSED, run.getStatus());
        assertThat(run.getDescription()).contains("passed");
    }

    @Test
    @DisplayName("parseTestResults setzt Status SKIPPED wenn keine Tests liefen")
    void parseTestResultsMarksSkippedWhenNoTestsExecuted () throws Exception
    {
        String json = "{\"stats\":{\"unexpected\":0,\"expected\":0,\"skipped\":2,\"flaky\":0}}";
        TestRun run = new TestRun();

        invokeParse(run, json, 2);

        assertEquals(TestStatus.SKIPPED, run.getStatus());
        assertThat(run.getDescription()).contains("No tests were executed");
    }

    @Test
    @DisplayName("parseTestResults bleibt robust bei ungültigem JSON")
    void parseTestResultsHandlesInvalidJson () throws Exception
    {
        TestRun run = new TestRun();

        invokeParse(run, "{\"stats\": invalid}", 0);

        assertEquals(TestStatus.PASSED, run.getStatus());
        assertThat(run.getDescription()).contains("exit code 0");
    }

    @Test
    @DisplayName("buildCommand nutzt Windows Aufruf mit cmd.exe")
    void buildCommandUsesWindowsSyntax () throws Exception
    {
        System.setProperty("os.name", "Windows 11");

        @SuppressWarnings("unchecked")
        List<String> command = (List<String>) buildCommandMethod.invoke(runner, "login.spec.ts");

        assertThat(command).hasSizeGreaterThanOrEqualTo(3);
        assertThat(command.get(0)).isEqualTo("cmd.exe");
        assertThat(command.get(1)).isEqualTo("/c");
        assertThat(command.get(2)).contains("cd backend");
        assertThat(command.get(2)).contains("npx playwright test login.spec.ts");
    }

    private void invokeParse (TestRun run, String output, int exitCode) throws Exception
    {
        try
        {
            parseMethod.invoke(runner, run, output, exitCode);
        } catch (InvocationTargetException ex)
        {
            if (ex.getCause() instanceof Exception cause)
            {
                throw cause;
            }
            throw ex;
        }
    }
}
