import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Test, TestRun} from '../models';

@Injectable({
  providedIn: 'root'
})
export class TestService {

  private backendUrl = "http://localhost:8080";

  constructor(private http: HttpClient) {
  }

  /** Get all tests for a given user story */
  getTestsByUserStoryId(userStoryId: number): Observable<HttpResponse<Test[]>> {
    return this.http.get<Test[]>(`${this.backendUrl}/getTests/${userStoryId}`, {observe: 'response'});
  }

  /** Get a test by its ID */
  getTestById(id: number): Observable<HttpResponse<Test>> {
    return this.http.get<Test>(`${this.backendUrl}/test/${id}`, {observe: 'response'});
  }

  /** Create a new test */
  createTest(test: Test): Observable<HttpResponse<Test>> {
    return this.http.post<Test>(`${this.backendUrl}/test`, test, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      observe: 'response'
    });
  }

  /** Update an existing test */
  updateTest(id: number, test: Test): Observable<HttpResponse<Test>> {
    return this.http.patch<Test>(`${this.backendUrl}/test/${id}`, test, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      observe: 'response'
    });
  }

  /** Delete a test by its ID */
  deleteTest(id: number): Observable<HttpResponse<void>> {
    return this.http.delete<void>(`${this.backendUrl}/test/${id}`, {observe: 'response'});
  }

  /** Trigger test generation for a given test ID */
  generateTest(id: number) {
    return this.http.post<void>(`${this.backendUrl}/generate/${id}`, {}, {observe: 'response'});
  }

  /** Get the generated test code for a given test ID */
  getTestCode(id: number): Observable<HttpResponse<any>> {
    return this.http.get(`${this.backendUrl}/test/code/${id}`, {observe: 'response'});
  }

  /** Save the test code for a given test ID */
  saveTestCode(id: number, code: string): Observable<HttpResponse<string>> {
    return this.http.put(`${this.backendUrl}/test/code/${id}`, {testID: id, code: code}, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      observe: 'response',
      responseType: 'text'
    });
  }

  /** Execute Playwright test for a given test ID */
  executeTest(id: number): Observable<HttpResponse<TestRun>> {
    return this.http.post<TestRun>(`${this.backendUrl}/test/execute/${id}`, {}, {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      observe: 'response'
    });
  }
}
