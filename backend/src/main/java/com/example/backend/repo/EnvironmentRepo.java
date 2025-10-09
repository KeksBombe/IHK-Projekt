package com.example.backend.repo;


import com.example.backend.models.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EnvironmentRepo extends JpaRepository<Environment, Long>
{

    @Query("SELECT e FROM Environment e WHERE e.projectID = ?1")
    List<Environment> findByProjectId (Long projectId);
}
