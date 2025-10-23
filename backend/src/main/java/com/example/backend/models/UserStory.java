package com.example.backend.models;


import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class UserStory
{

    public UserStory ()
    {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectid", referencedColumnName = "id")
    private Project project;

}
