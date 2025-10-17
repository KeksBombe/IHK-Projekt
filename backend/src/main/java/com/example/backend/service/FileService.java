package com.example.backend.service;


import com.example.backend.constants.EFileType;
import com.example.backend.interfaces.IFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


@Service
@Slf4j
public class FileService implements IFileService
{

    private static final Path BASE_PATH = Path.of("backend/playwright_tests");

    public FileService () throws IOException
    {
        if (!Files.exists(BASE_PATH))
        {
            Files.createDirectories(BASE_PATH);
        }
        log.info("Tests will be stored in: {}", BASE_PATH.toAbsolutePath());
        log.info("Currently contains {} files", Files.list(BASE_PATH).count());
    }

    @Override
    public Path writeFile (String fileName, String content, EFileType fileType) throws IOException
    {
        Path filePath = BASE_PATH.resolve(fileName + fileType.getExtension());

        Files.writeString(filePath,
                content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        return filePath;
    }

    @Override
    public String readFile (String fileName) throws IOException
    {
        Path file = BASE_PATH.resolve(fileName);
        log.debug("Reading file {}", file.toAbsolutePath());
        if (!Files.exists(file))
        {
            throw new NoSuchFileException("File not found: " + fileName);
        }
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    public Path getFilePath (String fileName, EFileType fileType)
    {
        return BASE_PATH.resolve(fileName + fileType.getExtension());
    }
}
