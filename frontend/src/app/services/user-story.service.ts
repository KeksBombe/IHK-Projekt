import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserStory} from '../models';

@Injectable({
  providedIn: 'root'
})
export class UserStoryService {

  private backendUrl = "http://localhost:8080";

  constructor(private http: HttpClient) {
  }

  /** Get all user stories for a given project */
  getUserStories(projectId: number): Observable<UserStory[]> {
    return this.http.get<UserStory[]>(`${this.backendUrl}/getUserStories/${projectId}`);
  }

  /** Create a new user story */
  createUserStory(userStory: UserStory): Observable<UserStory> {
    return this.http.post<UserStory>(`${this.backendUrl}/userStory`, userStory, {
      headers: new HttpHeaders({'Content-Type': 'application/json'})
    });
  }

  /** Delete a user story by its ID */
  deleteUserStory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.backendUrl}/deleteUserStory/${id}`);
  }
}
