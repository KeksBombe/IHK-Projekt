package com.example.backend.models;


import com.example.backend.constants.TestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private TestStatus status;

    private String description;
    
    @Column(name = "executed_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime executedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "testid", referencedColumnName = "id")
    private TestModel test;

    @PrePersist
    public void prePersist ()
    {
        if (this.executedAt == null)
        {
            this.executedAt = LocalDateTime.now();
        }
    }

}
