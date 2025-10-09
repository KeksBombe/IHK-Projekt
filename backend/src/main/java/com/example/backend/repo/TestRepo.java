package com.example.backend.repo;


import com.example.backend.models.TestModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TestRepo extends JpaRepository<TestModel, Long>
{
    List<TestModel> findByStoryID (Long storyId);
}
