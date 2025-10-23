import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TestRun} from '../models';

@Injectable({
  providedIn: 'root'
})
export class TestRunService {

  private backendUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {
  }

  getTestRuns(testId: number): Observable<HttpResponse<TestRun[]>> {
    return this.http.get<TestRun[]>(`${this.backendUrl}/test/${testId}/runs`, {observe: 'response'});
  }
}
