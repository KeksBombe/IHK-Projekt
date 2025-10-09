package com.example.backend.controller;


import com.example.backend.models.TestModel;
import com.example.backend.repo.TestRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
public class TestController
{

    private final TestRepo testRepo;

    public TestController (TestRepo testRepo)
    {
        this.testRepo = testRepo;
    }

    @GetMapping("/getTests/{id}")
    public ResponseEntity<List<TestModel>> getTests (@PathVariable Long id)
    {
        try
        {
            List<TestModel> tests = testRepo.findByStoryID(id);
            return tests.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(tests);
        } catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/test")
    public ResponseEntity<TestModel> createTest (@RequestBody TestModel test)
    {
        try
        {
            test.setId(null);
            TestModel testObj = testRepo.save(test);
            return ResponseEntity.status(HttpStatus.CREATED).body(testObj);
        } catch (Exception e)
        {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/test/{id}")
    public ResponseEntity<TestModel> updateTest (@PathVariable Long id, @RequestBody TestModel test)
    {
        try
        {
            if (testRepo.existsById(id))
            {
                test.setId(id);
                TestModel testObj = testRepo.save(test);
                return ResponseEntity.ok(testObj);
            } else
            {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e)
        {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/test/{id}")
    public ResponseEntity<HttpStatus> deleteTest (@PathVariable Long id)
    {
        try
        {
            if (testRepo.existsById(id))
            {
                testRepo.deleteById(id);
                return ResponseEntity.ok().build();
            } else
            {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }
}
