package com.example.backend.exceptions;


public class FileOperationException extends RuntimeException
{

    public FileOperationException (String message, Throwable cause)
    {
        super(message, cause);
    }
}
