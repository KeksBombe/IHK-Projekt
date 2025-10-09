import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
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
  getTestsByUserStoryId(userStoryId: number): Observable<HttpResponse<Test[]>> {
    return this.http.get<Test[]>(`${this.backendUrl}/getTests/${userStoryId}`, {observe: 'response'});
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
    return this.http.delete<void>(`${this.backendUrl}/deleteTest/${id}`, {observe: 'response'});
  }
}
