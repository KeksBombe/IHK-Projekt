import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Environment} from '../models';


@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {
  }

  getEnvironmentById(id: number): Observable<HttpResponse<Environment>> {
    return this.http.get<Environment>(`${this.baseUrl}/getEnvironmentById/${id}`, {observe: 'response'});
  }

  createEnvironment(environment: Environment): Observable<HttpResponse<Environment>> {
    return this.http.post<Environment>(`${this.baseUrl}/createEnvironment`, environment, {observe: 'response'});
  }

  updateEnvironment(id: number, environment: Environment): Observable<HttpResponse<Environment>> {
    return this.http.patch<Environment>(`${this.baseUrl}/updateEnvironment/${id}`, environment, {observe: 'response'});
  }

  deleteEnvironment(id: number): Observable<HttpResponse<void>> {
    return this.http.delete<void>(`${this.baseUrl}/deleteEnvironment/${id}`, {observe: 'response'});
  }

  getEnvironmentsByProjectId(projectId: number): Observable<HttpResponse<Environment[]>> {
    return this.http.get<Environment[]>(`${this.baseUrl}/getEnvironmentsByProjectId/${projectId}`, {observe: 'response'});
  }
}
