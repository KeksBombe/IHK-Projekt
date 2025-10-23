package com.example.backend.controller;


import com.example.backend.constants.EFileType;
import com.example.backend.dto.CreateTestRequest;
import com.example.backend.dto.TestDto;
import com.example.backend.dto.TestRunDto;
import com.example.backend.exceptions.GenerationException;
import com.example.backend.mapper.TestMapper;
import com.example.backend.mapper.TestRunMapper;
import com.example.backend.models.PlaywrightTest;
import com.example.backend.models.TestModel;
import com.example.backend.models.TestRun;
import com.example.backend.repo.TestRepo;
import com.example.backend.service.AiService;
import com.example.backend.service.FileService;
import com.example.backend.service.PlaywrightTestRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TestControllerTest
{

    @Mock
    private TestRepo testRepo;

    @Mock
    private TestMapper testMapper;

    @Mock
    private TestRunMapper testRunMapper;

    @Mock
    private AiService aiService;

    @Mock
    private FileService fileService;

    @Mock
    private PlaywrightTestRunner playwrightTestRunner;

    @InjectMocks
    private TestController testController;

    private TestModel testModel;
    private TestDto testDto;

    @BeforeEach
    void setUp ()
    {
        testModel = new TestModel();
        testModel.setId(11L);
        testModel.setName("Smoke Test");
        testModel.setTestCSV("step,action,expected\n1,open,'done'");

        testDto = new TestDto();
        testDto.setId(11L);
        testDto.setName("Smoke Test");
    }

    @Test
    @DisplayName("getTests liefert 200 mit vorhandenen Tests")
    void getTestsReturnsOkWhenTestsExist ()
    {
        when(testRepo.findByStoryID(7L)).thenReturn(List.of(testModel));
        when(testMapper.toDto(testModel)).thenReturn(testDto);

        ResponseEntity<List<TestDto>> response = testController.getTests(7L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<TestDto> tests = response.getBody();
        assertThat(tests).hasSize(1);
    }

    @Test
    @DisplayName("getTests liefert 204 wenn keine Tests existieren")
    void getTestsReturnsNoContentWhenEmpty ()
    {
        when(testRepo.findByStoryID(7L)).thenReturn(List.of());

        ResponseEntity<List<TestDto>> response = testController.getTests(7L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("getTestById liefert 200 wenn Test vorhanden")
    void getTestByIdReturnsOkWhenFound ()
    {
        when(testRepo.findById(11L)).thenReturn(Optional.of(testModel));
        when(testMapper.toDto(testModel)).thenReturn(testDto);

        ResponseEntity<TestDto> response = testController.getTestById(11L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("createTest setzt ID auf null und liefert 201")
    void createTestResetsIdAndReturnsCreated ()
    {
        CreateTestRequest request = new CreateTestRequest();
        request.setName("Edge Test");
        request.setStoryID(7L);

        TestModel newTest = new TestModel();
        newTest.setName("Edge Test");

        TestModel savedTest = new TestModel();
        savedTest.setId(22L);
        savedTest.setName("Edge Test");

        TestDto savedDto = new TestDto();
        savedDto.setId(22L);
        savedDto.setName("Edge Test");

        when(testMapper.toEntity(request)).thenReturn(newTest);
        when(testRepo.save(newTest)).thenReturn(savedTest);
        when(testMapper.toDto(savedTest)).thenReturn(savedDto);

        ResponseEntity<TestDto> response = testController.createTest(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        TestDto created = response.getBody();
        assertEquals(22L, created.getId());
    }

    @Test
    @DisplayName("updateTest überschreibt vorhandenen Test")
    void updateTestReturnsOkWhenExists ()
    {
        TestDto updateDto = new TestDto();
        updateDto.setId(11L);
        updateDto.setName("Updated");
        updateDto.setStoryID(7L);

        TestModel updatedEntity = new TestModel();
        updatedEntity.setId(11L);
        updatedEntity.setName("Updated");

        TestModel savedTest = new TestModel();
        savedTest.setId(11L);
        savedTest.setName("Updated");

        TestDto savedDto = new TestDto();
        savedDto.setId(11L);
        savedDto.setName("Updated");

        when(testRepo.existsById(11L)).thenReturn(true);
        when(testMapper.toEntity(updateDto)).thenReturn(updatedEntity);
        when(testRepo.save(updatedEntity)).thenReturn(savedTest);
        when(testMapper.toDto(savedTest)).thenReturn(savedDto);

        ResponseEntity<TestDto> response = testController.updateTest(11L, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestDto persisted = response.getBody();
        assertEquals("Updated", persisted.getName());
    }

    @Test
    @DisplayName("deleteTest entfernt Test wenn vorhanden")
    void deleteTestReturnsOkWhenExists ()
    {
        when(testRepo.existsById(11L)).thenReturn(true);
        doNothing().when(testRepo).deleteById(11L);

        ResponseEntity<Void> response = testController.deleteTest(11L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(testRepo).deleteById(11L);
    }

    @Test
    @DisplayName("generateTest ruft AI-Service und speichert Datei")
    void generateTestReturnsOkAndWritesFile () throws Exception
    {
        when(testRepo.findById(11L)).thenReturn(Optional.of(testModel));
        when(aiService.generateAndRunTests(testModel)).thenReturn("generated code");

        ResponseEntity<String> response = testController.generateTest(11L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("generated code", response.getBody());
        verify(fileService).writeFile(String.valueOf(testModel.getId()), "generated code", EFileType.SPEC_TS);
    }

    @Test
    @DisplayName("getTestCodeById liefert gespeicherten Code")
    void getTestCodeByIdReturnsSavedCode () throws Exception
    {
        String expected = "import { test } from '@playwright/test';";
        when(fileService.readFile("11" + EFileType.SPEC_TS.getExtension())).thenReturn(expected);

        ResponseEntity<PlaywrightTest> response = testController.getTestCodeById(11L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PlaywrightTest body = response.getBody();
        assertEquals(expected, body.getCode());
        assertEquals(11L, body.getTestID());
    }

    @Test
    @DisplayName("getTestCodeById liefert 204 wenn Datei fehlt")
    void getTestCodeByIdReturnsNoContentWhenFileMissing () throws Exception
    {
        when(fileService.readFile("11" + EFileType.SPEC_TS.getExtension())).thenThrow(new IOException("missing"));

        ResponseEntity<PlaywrightTest> response = testController.getTestCodeById(11L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("saveTestCodeById speichert Code erfolgreich")
    void saveTestCodeByIdReturnsOkOnSuccess () throws Exception
    {
        when(testRepo.existsById(11L)).thenReturn(true);
        PlaywrightTest payload = new PlaywrightTest();
        payload.setCode("console.log('ok');");

        ResponseEntity<String> response = testController.saveTestCodeById(11L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(fileService).writeFile("11", payload.getCode(), EFileType.SPEC_TS);
    }

    @Test
    @DisplayName("executePlaywrightTest führt Test aus und gibt TestRunDto zurück")
    void executePlaywrightTestReturnsTestRunDto ()
    {
        TestRun testRun = new TestRun();
        testRun.setId(100L);

        TestRunDto testRunDto = new TestRunDto();
        testRunDto.setId(100L);

        when(testRepo.existsById(11L)).thenReturn(true);
        when(playwrightTestRunner.runPlaywrightTest(11L, "11.spec.ts")).thenReturn(testRun);
        when(testRunMapper.toDto(testRun)).thenReturn(testRunDto);

        ResponseEntity<TestRunDto> response = testController.executePlaywrightTest(11L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        assertEquals(100L, response.getBody().getId());
    }
}