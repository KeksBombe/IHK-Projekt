import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Environment} from '../models';


@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {
  }

  getEnvironmentById(id: number): Observable<Environment> {
    return this.http.get<Environment>(`${this.baseUrl}/getEnvironmentById/${id}`);
  }

  createEnvironment(environment: Environment): Observable<Environment> {
    return this.http.post<Environment>(`${this.baseUrl}/createEnvironment`, environment);
  }

  updateEnvironment(id: number, environment: Environment): Observable<Environment> {
    return this.http.patch<Environment>(`${this.baseUrl}/updateEnvironment/${id}`, environment);
  }

  deleteEnvironment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/deleteEnvironment/${id}`);
  }

  getEnvironmentsByProjectId(projectId: number): Observable<Environment[]> {
    return this.http.get<Environment[]>(`${this.baseUrl}/getEnvironmentsByProjectId/${projectId}`);
  }
}
