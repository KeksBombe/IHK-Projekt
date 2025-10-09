import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserStory} from '../models';

@Injectable({
  providedIn: 'root'
})
export class UserStoryService {

  private backendUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {
  }

  /** Get all user stories for a given project */
  getUserStories(projectId: number): Observable<HttpResponse<UserStory[]>> {
    return this.http.get<UserStory[]>(
      `${this.backendUrl}/getUserStories/${projectId}`,
      {
        headers: new HttpHeaders({'Content-Type': 'application/json'}),
        observe: 'response'
      }
    );
  }

  /** Create a new user story */
  createUserStory(userStory: UserStory): Observable<HttpResponse<UserStory>> {
    return this.http.post<UserStory>(
      `${this.backendUrl}/userStory`,
      userStory,
      {
        headers: new HttpHeaders({'Content-Type': 'application/json'}),
        observe: 'response'
      }
    );
  }

  /** Update an existing user story */
  updateUserStory(userStory: UserStory): Observable<HttpResponse<UserStory>> {
    return this.http.patch<UserStory>(
      `${this.backendUrl}/userStory/${userStory.id}`,
      userStory,
      {
        headers: new HttpHeaders({'Content-Type': 'application/json'}),
        observe: 'response'
      }
    );
  }

  /** Delete a user story by its ID */
  deleteUserStory(id: number): Observable<HttpResponse<void>> {
    return this.http.delete<void>(
      `${this.backendUrl}/deleteUserStory/${id}`,
      {
        headers: new HttpHeaders({'Content-Type': 'application/json'}),
        observe: 'response'
      }
    );
  }
}
