package com.example.backend.service;


import com.example.backend.exceptions.GenerationException;
import com.example.backend.dto.TestDto;
import com.example.backend.models.Environment;
import com.example.backend.models.GenerationState;
import com.example.backend.models.TestModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AiService
{

    private final ChatClient chatClient;

    public AiService (ChatClient.Builder builder, ToolCallbackProvider tools)
    {
        this.chatClient = builder
                .defaultToolCallbacks(tools)
                .build();
    }

    public String generateAndRunTests (TestModel test) throws GenerationException
    {
        try
        {
            test.setGenerationState(GenerationState.IN_PROGRESS);
            String testStepsCSV = TestDto.stepsToCsv(test.getSteps());
            boolean hasEnv = test.getEnvironment() != null;

            if (testStepsCSV == null || testStepsCSV.trim().isEmpty())
            {
                log.warn("No test steps CSV found for test ID: {}", test.getId());
                return "No test steps provided";
            }

            log.info("Generating Playwright tests for test ID: {}", test.getId());

            // Create the prompt with system message and test steps as CSV
            String systemPrompt =
                    "You are a world-class, thorough test automation agent. Your job is to create *robust and reliable* test scripts based on user instructions. You must act like a human tester and not a simple machine.\n" +
                            "\n" +
                            "1. **EXECUTE STEPS:** Execute all test steps from the user's CSV using Playwright tools.\n" +
                            "\n" +
                            "2. **CRITICAL - HANDLE DYNAMIC CONTENT:** The web is unpredictable. You **MUST** watch for and handle unexpected elements like **cookie consent popups, login dialogs, or special offers.** " +
                            "If one appears, interact with it to clear the screen (e.g., click 'Accept' or 'Reject') *before* continuing to the user's next step. **If you had to click a popup away then you MUST include these interactions in your final script.**\n" +
                            "When no popup is detected DO NOT INCLUDE IT IN THE FINAL SCRIPT.\n" +
                            "\n" +
                            "3. **CRITICAL - VALIDATE ASSERTIONS:** When a test step has an 'Expected Result', your job is to **VALIDATE** it.\n" +
                            "* First, use Playwright tools to get the **ACTUAL** value from the page (e.g., get the text of an element).\n" +
                            "* Then, use an `expect()` assertion to **compare the ACTUAL value to the EXPECTED value** from the CSV.\n" +
                            "* **ABSOLUTE RULE - DO NOT 'FIX' TESTS: If the ACTUAL value (e.g., \"Reitsportgemeinschaft\") is different from the EXPECTED value (e.g., \"Reitsportverein\"), you MUST write the assertion to check for the original EXPECTED value. The test is supposed to fail in this case. Do not alter the assertion to match the actual value.**\n" +
                            "* Do NOT just copy the expected value into an assertion without checking it first. " +
                            "\n" +
                            "4. **STABILITY:**\n" +
                            "- Include appropriate wait times and assertions.\n" +
                            "- If a test step cannot be executed, comment the reason in the file.\n" +
                            "\n" +
                            "5. **FINAL SEQUENCE:**\n" +
                            "* Execute all steps (including implicit steps like handling popups).\n" +
                            "* Call the `browser.close()` method.\n" +
                            "* After the browser is closed, respond with **ONLY** the complete, runnable Playwright TypeScript script (starting with `import...`). This script must log **every single tool call you made**, including those for handling popups.";

            StringBuilder userMessage = new StringBuilder();
            if (hasEnv)
            {
                Environment env = test.getEnvironment();
                userMessage
                        .append("Environment:\n\n URL: ").append(env.getUrl())
                        .append("\n\nUsername: ").append(env.getUsername())
                        .append("\n\n Password: ").append(env.getPassword()).append("\n\n");
            }
            userMessage.append("Here are the test steps to execute in CSV format:\n\n").append(testStepsCSV);

            log.info("Generating with UserPrompt:\n{}", userMessage);

            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage.toString())
                    .call()
                    .content();

            log.info("AI response received for test ID: {}", test.getId());
            log.info(response);
            test.setGenerationState(GenerationState.COMPLETED);
            return response;

        } catch (Exception e)
        {
            log.error("Error generating tests for test ID: {}", test.getId(), e);
            test.setGenerationState(GenerationState.FAILED);
            throw new GenerationException("Failed to generate and run tests: " + e.getMessage());
        }
    }
}
