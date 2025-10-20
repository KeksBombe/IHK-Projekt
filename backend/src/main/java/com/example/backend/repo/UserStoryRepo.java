package com.example.backend.repo;


import com.example.backend.models.UserStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserStoryRepo extends JpaRepository<UserStory, Long>
{
    @Query("SELECT u FROM UserStory u WHERE u.project.id = ?1")
    List<UserStory> findByProjectID (Long projectId);
}
