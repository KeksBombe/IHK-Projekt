package com.example.backend.models;


import com.example.backend.constants.TestStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.net.URL;
import java.time.LocalDateTime;


@Entity
@Data
public class TestRun
{

    public TestRun ()
    {

    }

    //Attribute
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    TestStatus status;
    String description;
    URL linkToSkript;
    LocalDateTime startTime;

    //Fremdschlüssel
    Long testID;

}
