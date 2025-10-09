import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Agent} from '../models/aiagent';

export interface AgentCallRequest {
  name: string;
  instructions: string;
  prompt: string;
}

export interface AgentCallResponse {
  answer: string;
}

const backendUrl = "http://localhost:8080";

@Injectable({providedIn: 'root'})
export class AIService {
  private readonly baseUrl = `${backendUrl.replace(/\/$/, '')}/api/ai`;

  constructor(private http: HttpClient) {
  }

  callAi(agent: Agent, prompt: string): Observable<AgentCallResponse> {
    const body: AgentCallRequest = {name: agent.name, instructions: agent.instructions, prompt};
    return this.http.post<AgentCallResponse>(`${this.baseUrl}/call`, body);
  }
}
