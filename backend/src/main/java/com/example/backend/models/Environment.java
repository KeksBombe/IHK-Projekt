package com.example.backend.models;


import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class Environment
{
    public Environment ()
    {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String username;

    private String password;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectid", referencedColumnName = "id")
    private Project project;

}
