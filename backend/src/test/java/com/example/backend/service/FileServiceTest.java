package com.example.backend.service;


import com.example.backend.constants.EFileType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class FileServiceTest
{

    private final List<Path> createdFiles = new ArrayList<>();

    @AfterEach
    void cleanUp ()
    {
        createdFiles.forEach(path ->
        {
            try
            {
                Files.deleteIfExists(path);
            } catch (IOException ignored)
            {
                // Ignore cleanup failures to avoid masking test results
            }
        });
        createdFiles.clear();
    }

    @Test
    @DisplayName("writeFile legt Datei mit Inhalt an")
    void writeFileCreatesFileWithContent () throws Exception
    {
        FileService fileService = new FileService();
        String fileName = "unit-write-" + System.nanoTime();
        String content = "console.log('hello');";

        Path path = fileService.writeFile(fileName, content, EFileType.SPEC_TS);
        createdFiles.add(path);

        assertThat(Files.exists(path)).isTrue();
        assertEquals(content, Files.readString(path));
    }

    @Test
    @DisplayName("readFile liefert gespeicherten Inhalt")
    void readFileReturnsStoredContent () throws Exception
    {
        FileService fileService = new FileService();
        String fileName = "unit-read-" + System.nanoTime();
        String fullFileName = fileName + EFileType.SPEC_TS.getExtension();
        Path fullPath = fileService.writeFile(fileName, "const x = 1;", EFileType.SPEC_TS);
        createdFiles.add(fullPath);

        String content = fileService.readFile(fullFileName);

        assertEquals("const x = 1;", content);
    }

    @Test
    @DisplayName("readFile wirft NoSuchFileException bei unbekannter Datei")
    void readFileThrowsWhenFileMissing () throws Exception
    {
        FileService fileService = new FileService();
        String missingFile = "missing-" + System.nanoTime() + EFileType.SPEC_TS.getExtension();

        assertThrows(NoSuchFileException.class, () -> fileService.readFile(missingFile));
    }
}
