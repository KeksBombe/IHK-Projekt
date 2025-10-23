package com.example.backend.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class TestModel
{

    public TestModel ()
    {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String description;
    @Column(columnDefinition = "TEXT")
    String testCSV;

    //Fremdschlüssel
    @ManyToOne
    @JoinColumn(name = "environmentid", referencedColumnName = "id")
    @JsonIgnore
    Environment environment;

    @ManyToOne
    @JoinColumn(name = "storyid", referencedColumnName = "id")
    @JsonIgnore
    UserStory userStory;

    @Enumerated(EnumType.STRING)
    GenerationState generationState = GenerationState.NOT_STARTED;

    // JSON serialization compatibility
    @JsonProperty("environmentID")
    public Long getEnvironmentID ()
    {
        return environment != null ? environment.getId() : null;
    }

    @JsonProperty("environmentID")
    public void setEnvironmentID (Long environmentID)
    {
        if (environmentID != null)
        {
            Environment e = new Environment();
            e.setId(environmentID);
            this.environment = e;
        } else
        {
            this.environment = null;
        }
    }

    @JsonProperty("storyID")
    public Long getStoryID ()
    {
        return userStory != null ? userStory.getId() : null;
    }

    @JsonProperty("storyID")
    public void setStoryID (Long storyID)
    {
        if (storyID != null)
        {
            UserStory us = new UserStory();
            us.setId(storyID);
            this.userStory = us;
        }
    }
}
