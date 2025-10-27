package com.example.backend.dto;


import com.example.backend.models.GenerationState;
import com.example.backend.models.TestStep;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Data
public class TestDto
{

    private Long id;

    @NotBlank(message = "Test name is required")
    @Size(min = 1, max = 255, message = "Test name must be between 1 and 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private String testCSV;

    private Long environmentID;

    @NotNull(message = "Story ID is required")
    private Long storyID;

    private GenerationState generationState;

    // CSV conversion helpers live in DTO by design (keep model clean)
    public static List<TestStep> parseCsvToSteps (String csv)
    {
        List<TestStep> steps = new ArrayList<>();
        if (csv == null || csv.trim().isEmpty())
        {
            return steps;
        }
        try
        {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .setTrim(true)
                    .setAllowMissingColumnNames(true)
                    .setDelimiter(',')
                    .build()
                    .parse(new StringReader(csv));

            int idx = 0;
            for (CSVRecord rec : records)
            {
                TestStep ts = new TestStep();
                ts.setIndex(idx++);
                // Support both English and German headers
                String action = getFirstNonEmpty(rec, "Action", "Aktion");
                String data = getFirstNonEmpty(rec, "Data", "Daten");
                String expected = getFirstNonEmpty(rec, "Expected Result", "Erwartetes Resultat");
                ts.setAction(action != null ? action : "");
                ts.setData(data != null ? data : "");
                ts.setExpectedResult(expected != null ? expected : "");
                steps.add(ts);
            }
        } catch (IOException e)
        {
            // Should not happen with StringReader
            return steps;
        }
        return steps;
    }

    public static String stepsToCsv (List<TestStep> steps)
    {
        if (steps == null || steps.isEmpty())
        {
            return "Action,Data,Expected Result";
        }
        // Ensure deterministic order by index
        steps.sort(Comparator.comparing(s -> s.getIndex() == null ? Integer.MAX_VALUE : s.getIndex()));
        try (StringWriter sw = new StringWriter();
             CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
                     .setHeader("Action", "Data", "Expected Result")
                     .build()))
        {
            for (TestStep s : steps)
            {
                printer.printRecord(
                        safe(s.getAction()),
                        safe(s.getData()),
                        safe(s.getExpectedResult())
                );
            }
            printer.flush();
            return sw.toString();
        } catch (IOException e)
        {
            return "Action,Data,Expected Result";
        }
    }

    private static String getFirstNonEmpty (CSVRecord rec, String... headers)
    {
        for (String h : headers)
        {
            if (rec.isMapped(h))
            {
                String v = rec.get(h);
                if (v != null && !v.trim().isEmpty()) return v.trim();
            }
        }
        return null;
    }

    private static String safe (String v)
    {
        return v == null ? "" : v;
    }
}
