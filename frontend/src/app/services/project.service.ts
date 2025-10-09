import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Project} from '../models';

@Injectable({providedIn: 'root'})
export class ProjectService {

  private backendUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {
  }

  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.backendUrl}/getAllProjects`);
  }

  createProject(projectName: string): Observable<HttpResponse<Project>> {
    const project: Partial<Project> = {name: projectName};
    return this.http.post<Project>(`${this.backendUrl}/createProject`, project, {observe: 'response'});
  }

  getProjectById(projectId: number): Observable<Project> {
    return this.http.get<Project>(`${this.backendUrl}/getProjectById/${projectId}`);
  }

  renameProject(project: Project): Observable<HttpResponse<Project>> {
    return this.http.patch<Project>(`${this.backendUrl}/renameProject/${project.id}`, project, {observe: 'response'});
  }
}

