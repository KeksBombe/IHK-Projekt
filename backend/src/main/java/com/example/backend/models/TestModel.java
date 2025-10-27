package com.example.backend.models;


import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
public class TestModel
{

    public TestModel ()
    {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environmentid", referencedColumnName = "id")
    private Environment environment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storyid", referencedColumnName = "id")
    private UserStory userStory;

    @Enumerated(EnumType.STRING)
    private GenerationState generationState = GenerationState.NOT_STARTED;

    @OneToMany(mappedBy = "testModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestStep> steps = new ArrayList<>();

}
