package com.example.backend.models;


import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class TestStep
{
    public TestStep ()
    {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer index;

    private String action;

    private String data;

    private String expectedResult;

    @ManyToOne
    @JoinColumn(name = "testid", referencedColumnName = "id")
    private TestModel testModel;
}
