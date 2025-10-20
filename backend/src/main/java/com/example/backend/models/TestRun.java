package com.example.backend.models;


import com.example.backend.constants.TestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;


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

    //Fremdschlüssel
    @ManyToOne
    @JoinColumn(name = "testid", referencedColumnName = "id")
    @JsonIgnore
    TestModel test;

    // JSON serialization compatibility
    @JsonProperty("testId")
    public Long getTestId ()
    {
        return test != null ? test.getId() : null;
    }

    @JsonProperty("testId")
    public void setTestId (Long testId)
    {
        if (testId != null)
        {
            TestModel t = new TestModel();
            t.setId(testId);
            this.test = t;
        }
    }

}
