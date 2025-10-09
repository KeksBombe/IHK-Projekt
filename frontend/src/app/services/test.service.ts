import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Test} from '../models';

@Injectable({
  providedIn: 'root'
})
export class TestService {

  private backendUrl = "http://localhost:8080";

  constructor(private http: HttpClient) {
  }

  /** Get all tests for a given user story */
  getTestsByUserStoryId(userStoryId: number): Observable<Test[]> {
    return this.http.get<Test[]>(`${this.backendUrl}/getTests/${userStoryId}`);
  }

  /** Create a new test */
  createTest(test: Test): Observable<Test> {
    return this.http.post<Test>(`${this.backendUrl}/test`, test, {
      headers: new HttpHeaders({'Content-Type': 'application/json'})
    });
  }

  /** Update an existing test */
  updateTest(id: number, test: Test): Observable<Test> {
    return this.http.put<Test>(`${this.backendUrl}/test/${id}`, test, {
      headers: new HttpHeaders({'Content-Type': 'application/json'})
    });
  }

  /** Delete a test by its ID */
  deleteTest(id: number): Observable<void> {
    return this.http.delete<void>(`${this.backendUrl}/deleteTest/${id}`);
  }
}

