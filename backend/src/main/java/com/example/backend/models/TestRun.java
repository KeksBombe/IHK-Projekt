package com.example.backend.models;


import com.example.backend.constants.TestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

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
    @Enumerated(EnumType.STRING)
    TestStatus status;
    String description;
    @Column(name = "executed_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime executedAt;

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

    @PrePersist
    public void prePersist ()
    {
        if (this.executedAt == null)
        {
            this.executedAt = LocalDateTime.now();
        }
    }

}
