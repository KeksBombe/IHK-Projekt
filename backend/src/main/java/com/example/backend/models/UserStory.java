package com.example.backend.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class UserStory
{

    public UserStory ()
    {

    }

    //Attribute
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String description;

    //Fremdschlüssel
    @ManyToOne
    @JoinColumn(name = "projectid", referencedColumnName = "id")
    @JsonIgnore
    Project project;

    // JSON serialization compatibility
    @JsonProperty("projectID")
    public Long getProjectID ()
    {
        return project != null ? project.getId() : null;
    }

    @JsonProperty("projectID")
    public void setProjectID (Long projectID)
    {
        if (projectID != null)
        {
            Project p = new Project();
            p.setId(projectID);
            this.project = p;
        }
    }

}
