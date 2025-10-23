package com.example.backend.controller;


import com.example.backend.constants.EFileType;
import com.example.backend.exceptions.GenerationException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TestControllerTest
{

    @Mock
    private TestRepo testRepo;

    @Mock
    private AiService aiService;

    @Mock
    private FileService fileService;

    @Mock
    private PlaywrightTestRunner playwrightTestRunner;

    @InjectMocks
    private TestController testController;

    private TestModel testModel;

    @BeforeEach
    void setUp ()
    {
        testModel = new TestModel();
        testModel.setId(11L);
        testModel.setName("Smoke Test");
        testModel.setTestCSV("step,action,expected\n1,open,'done'");
    }

    @Test
    @DisplayName("getTests liefert 200 mit vorhandenen Tests")
    void getTestsReturnsOkWhenTestsExist ()
    {
        when(testRepo.findByStoryID(7L)).thenReturn(List.of(testModel));

        ResponseEntity<List<TestModel>> response = testController.getTests(7L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<TestModel> tests = Objects.requireNonNull(response.getBody());
        assertThat(tests).containsExactly(testModel);
    }

    @Test
    @DisplayName("getTests liefert 204 wenn keine Tests existieren")
    void getTestsReturnsNoContentWhenEmpty ()
    {
        when(testRepo.findByStoryID(7L)).thenReturn(List.of());

        ResponseEntity<List<TestModel>> response = testController.getTests(7L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("getTests liefert 500 bei Fehler")
    void getTestsReturnsInternalServerErrorOnException ()
    {
        when(testRepo.findByStoryID(7L)).thenThrow(new RuntimeException("timeout"));

        ResponseEntity<List<TestModel>> response = testController.getTests(7L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("getTestById liefert 200 wenn Test vorhanden")
    void getTestByIdReturnsOkWhenFound ()
    {
        when(testRepo.findById(11L)).thenReturn(Optional.of(testModel));

        ResponseEntity<TestModel> response = testController.getTestById(11L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(Objects.requireNonNull(response.getBody())).isEqualTo(testModel);
    }

    @Test
    @DisplayName("getTestById liefert 204 wenn Test fehlt")
    void getTestByIdReturnsNoContentWhenMissing ()
    {
        when(testRepo.findById(11L)).thenReturn(Optional.empty());

        ResponseEntity<TestModel> response = testController.getTestById(11L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("getTestById liefert 500 bei Fehler")
    void getTestByIdReturnsInternalServerErrorOnException ()
    {
        when(testRepo.findById(11L)).thenThrow(new RuntimeException("db"));

        ResponseEntity<TestModel> response = testController.getTestById(11L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("createTest setzt ID auf null und liefert 201")
    void createTestResetsIdAndReturnsCreated ()
    {
        TestModel request = new TestModel();
        request.setId(99L);
        request.setName("Edge Test");

        when(testRepo.save(any(TestModel.class))).thenAnswer(invocation ->
        {
            TestModel toStore = invocation.getArgument(0);
            TestModel persisted = new TestModel();
            persisted.setId(22L);
            persisted.setName(toStore.getName());
            persisted.setDescription(toStore.getDescription());
            persisted.setTestCSV(toStore.getTestCSV());
            return persisted;
        });

        ResponseEntity<TestModel> response = testController.createTest(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        TestModel created = Objects.requireNonNull(response.getBody());
        assertEquals(22L, created.getId());

        ArgumentCaptor<TestModel> captor = ArgumentCaptor.forClass(TestModel.class);
        verify(testRepo).save(captor.capture());
        assertNull(captor.getValue().getId());
    }

    @Test
    @DisplayName("createTest liefert 500 bei Fehler")
    void createTestReturnsInternalServerErrorOnException ()
    {
        when(testRepo.save(any(TestModel.class))).thenThrow(new RuntimeException("constraint"));

        ResponseEntity<TestModel> response = testController.createTest(new TestModel());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("updateTest überschreibt vorhandenen Test")
    void updateTestReturnsOkWhenExists ()
    {
        when(testRepo.existsById(11L)).thenReturn(true);
        when(testRepo.save(any(TestModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TestModel update = new TestModel();
        update.setName("Updated");

        ResponseEntity<TestModel> response = testController.updateTest(11L, update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestModel persisted = Objects.requireNonNull(response.getBody());
        assertEquals("Updated", persisted.getName());
        assertEquals(11L, persisted.getId());
    }

    @Test
    @DisplayName("updateTest liefert 204 wenn Test fehlt")
    void updateTestReturnsNoContentWhenMissing ()
    {
        when(testRepo.existsById(12L)).thenReturn(false);

        ResponseEntity<TestModel> response = testController.updateTest(12L, new TestModel());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(testRepo, never()).save(any(TestModel.class));
    }

    @Test
    @DisplayName("updateTest liefert 500 bei Fehler")
    void updateTestReturnsInternalServerErrorOnException ()
    {
        when(testRepo.existsById(12L)).thenThrow(new RuntimeException("locked"));

        ResponseEntity<TestModel> response = testController.updateTest(12L, new TestModel());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("deleteTest entfernt Test wenn vorhanden")
    void deleteTestReturnsOkWhenExists ()
    {
        when(testRepo.existsById(11L)).thenReturn(true);
        doNothing().when(testRepo).deleteById(11L);

        ResponseEntity<HttpStatus> response = testController.deleteTest(11L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(testRepo).deleteById(11L);
    }

    @Test
    @DisplayName("deleteTest liefert 204 wenn Test fehlt")
    void deleteTestReturnsNoContentWhenMissing ()
    {
        when(testRepo.existsById(11L)).thenReturn(false);

        ResponseEntity<HttpStatus> response = testController.deleteTest(11L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(testRepo, never()).deleteById(11L);
    }

    @Test
    @DisplayName("deleteTest liefert 500 bei Fehler")
    void deleteTestReturnsInternalServerErrorOnException ()
    {
        when(testRepo.existsById(11L)).thenReturn(true);
        doThrow(new RuntimeException("not allowed")).when(testRepo).deleteById(11L);

        ResponseEntity<HttpStatus> response = testController.deleteTest(11L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
    @DisplayName("generateTest liefert 404 wenn Test fehlt")
    void generateTestReturnsNotFoundWhenMissing ()
    {
        when(testRepo.findById(11L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = testController.generateTest(11L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertThat(Objects.requireNonNull(response.getBody())).contains("not found");
        verify(aiService, never()).generateAndRunTests(any(TestModel.class));
    }

    @Test
    @DisplayName("generateTest liefert 500 wenn AI-Service fehlschlägt")
    void generateTestReturnsInternalServerErrorWhenAiFails () throws Exception
    {
        when(testRepo.findById(11L)).thenReturn(Optional.of(testModel));
        when(aiService.generateAndRunTests(testModel)).thenThrow(new GenerationException("azure"));

        ResponseEntity<String> response = testController.generateTest(11L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertThat(Objects.requireNonNull(response.getBody())).contains("Error generating tests");
    }

    @Test
    @DisplayName("generateTest liefert 500 wenn Speichern der Datei fehlschlägt")
    void generateTestReturnsInternalServerErrorWhenWriteFails () throws Exception
    {
        when(testRepo.findById(11L)).thenReturn(Optional.of(testModel));
        when(aiService.generateAndRunTests(testModel)).thenReturn("generated code");
        doThrow(new IOException("disk full")).when(fileService).writeFile(eq("11"), eq("generated code"), eq(EFileType.SPEC_TS));

        ResponseEntity<String> response = testController.generateTest(11L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertThat(Objects.requireNonNull(response.getBody())).contains("Error generating tests");
    }

    @Test
    @DisplayName("generateTest liefert 500 wenn Repository Fehler wirft")
    void generateTestReturnsInternalServerErrorWhenRepositoryThrows ()
    {
        when(testRepo.findById(11L)).thenThrow(new RuntimeException("db"));

        ResponseEntity<String> response = testController.generateTest(11L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("getTestCodeById liefert gespeicherten Code")
    void getTestCodeByIdReturnsSavedCode () throws Exception
    {
        String expected = "import { test } from '@playwright/test';";
        when(fileService.readFile("11" + EFileType.SPEC_TS.getExtension())).thenReturn(expected);

        ResponseEntity<PlaywrightTest> response = testController.getTestCodeById(11L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PlaywrightTest body = Objects.requireNonNull(response.getBody());
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
    @DisplayName("getTestCodeById liefert 500 bei unbekanntem Fehler")
    void getTestCodeByIdReturnsInternalServerErrorOnException () throws Exception
    {
        when(fileService.readFile("11" + EFileType.SPEC_TS.getExtension())).thenThrow(new RuntimeException("disk"));

        ResponseEntity<PlaywrightTest> response = testController.getTestCodeById(11L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
    @DisplayName("saveTestCodeById liefert 404 wenn Test fehlt")
    void saveTestCodeByIdReturnsNotFoundWhenMissing ()
    {
        when(testRepo.existsById(11L)).thenReturn(false);

        ResponseEntity<String> response = testController.saveTestCodeById(11L, new PlaywrightTest());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(fileService);
    }

    @Test
    @DisplayName("saveTestCodeById liefert 500 wenn Schreiben fehlschlägt")
    void saveTestCodeByIdReturnsInternalServerErrorOnWriteFailure () throws Exception
    {
        when(testRepo.existsById(11L)).thenReturn(true);
        PlaywrightTest payload = new PlaywrightTest();
        payload.setCode("console.log('fail');");
        doThrow(new IOException("disk"))
                .when(fileService)
                .writeFile("11", payload.getCode(), EFileType.SPEC_TS);

        ResponseEntity<String> response = testController.saveTestCodeById(11L, payload);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertThat(Objects.requireNonNull(response.getBody())).contains("Error saving test code");
    }

    @Test
    @DisplayName("saveTestCodeById liefert 500 bei unbekanntem Fehler")
    void saveTestCodeByIdReturnsInternalServerErrorOnException () throws Exception
    {
        when(testRepo.existsById(11L)).thenReturn(true);
        PlaywrightTest payload = new PlaywrightTest();
        payload.setCode("console.log('error');");
        doThrow(new IllegalStateException("state"))
                .when(fileService)
                .writeFile("11", payload.getCode(), EFileType.SPEC_TS);

        ResponseEntity<String> response = testController.saveTestCodeById(11L, payload);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertThat(Objects.requireNonNull(response.getBody())).contains("Internal server error");
    }

    @Test
    @DisplayName("executePlaywrightTest liefert 404 wenn Test fehlt")
    void executePlaywrightTestReturnsNotFoundWhenMissing ()
    {
        when(testRepo.existsById(11L)).thenReturn(false);

        ResponseEntity<TestRun> response = testController.executePlaywrightTest(11L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(playwrightTestRunner, never()).runPlaywrightTest(any(), any());
    }

    @Test
    @DisplayName("executePlaywrightTest liefert 500 bei Fehler")
    void executePlaywrightTestReturnsInternalServerErrorOnException ()
    {
        when(testRepo.existsById(11L)).thenReturn(true);
        when(playwrightTestRunner.runPlaywrightTest(11L, "11" + EFileType.SPEC_TS.getExtension()))
                .thenThrow(new RuntimeException("process"));

        ResponseEntity<TestRun> response = testController.executePlaywrightTest(11L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
