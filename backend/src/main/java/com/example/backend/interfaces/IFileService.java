package com.example.backend.interfaces;


import com.example.backend.constants.EFileType;

import java.io.IOException;
import java.nio.file.Path;


public interface IFileService
{
    Path writeFile (String fileName, String content, EFileType fileType) throws IOException;

    String readFile (String fileName) throws IOException;
}
