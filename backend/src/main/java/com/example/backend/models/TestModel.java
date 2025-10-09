package com.example.backend.models;


import jakarta.persistence.*;
import lombok.Data;


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

}
