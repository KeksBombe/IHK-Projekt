package com.example.backend.repo;


import com.example.backend.models.TestModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TestRepo extends JpaRepository<TestModel, Long>
{
    @Query("SELECT t FROM TestModel t WHERE t.userStory.id = ?1")
    List<TestModel> findByStoryID (Long storyId);
}
