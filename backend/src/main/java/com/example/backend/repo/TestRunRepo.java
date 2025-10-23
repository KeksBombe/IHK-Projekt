package com.example.backend.repo;


import com.example.backend.models.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TestRunRepo extends JpaRepository<TestRun, Long>
{
    List<TestRun> findByTest_IdOrderByExecutedAtDesc (Long testId);
}
