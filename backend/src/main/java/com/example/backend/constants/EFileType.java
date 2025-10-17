package com.example.backend.constants;


public enum EFileType
{
    SPEC_TS(".spec.ts");

    private final String extension;

    EFileType (String extension)
    {
        this.extension = extension;
    }

    public String getExtension ()
    {
        return extension;
    }
}
