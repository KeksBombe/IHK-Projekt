package com.example.backend.models;


import com.example.backend.constants.TestStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Entity
@Data
public class TestModel
{

    public TestModel ()
    {

    }

    //Attribute
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String description;
    @Column(columnDefinition = "TEXT")
    String testCSV;

    //Fremdschlüssel
    Long environmentID;
    Long storyID;

    @Enumerated(EnumType.STRING)
    GenerationState generationState = GenerationState.NOT_STARTED;
}

