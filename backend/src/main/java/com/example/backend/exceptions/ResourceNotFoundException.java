package com.example.backend.exceptions;


public class ResourceNotFoundException extends RuntimeException
{

    public ResourceNotFoundException (String resourceName, Long id)
    {
        super(String.format("%s with ID %d not found", resourceName, id));
    }
}
