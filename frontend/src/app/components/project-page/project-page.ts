import {ChangeDetectorRef, Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterOutlet} from '@angular/router';
import {DataView} from 'primeng/dataview';
import {Dialog} from 'primeng/dialog';
import {InputText} from 'primeng/inputtext';
import {FormsModule} from '@angular/forms';
import {Project, Environment, UserStory} from '../../models';
import {ProjectService} from '../../services/project.service';
import {Button} from 'primeng/button';
import {PrimeTemplate} from 'primeng/api';
import {UserStoryService} from '../../services/user-story.service';
import {storyItem} from './story-item/story-item';
import {ProjectSearch} from '../landing-page/project-search/project-search';
import {EnvironmentService} from '../../services/environment.service';
import {envItem} from './env-item/env-item';
import {Message} from 'primeng/message';
import {Card} from 'primeng/card';

@Component({
  selector: 'app-project-page',
  standalone: true,
  imports: [
    RouterOutlet,
    DataView,
    Dialog,
    InputText,
    FormsModule,
    Button,
    PrimeTemplate,
    storyItem,
    ProjectSearch,
    envItem,
    Message,
    Card
  ],
  templateUrl: './project-page.html',
  styleUrl: './project-page.scss'
})
export class ProjectPage implements OnInit {
  readonly projectId: number;
  private route = inject(ActivatedRoute);
  private projectService = inject(ProjectService);
  private userStoryService = inject(UserStoryService);
  private envService = inject(EnvironmentService);
  private cdr = inject(ChangeDetectorRef);

  currentProject: Project = {id: -1, name: ''};

  userStories: UserStory[] = [];
  environments: Environment[] = [];

  // Dialog state and form fields
  showAddUserStory = false;
  newUserStoryName = '';
  newUserStoryDescription = '';

  showAddEnvironment = false;
  newEnvironmentName = '';
  newEnvironmentUrl = '';
  newEnvironmentUsername = '';
  newEnvironmentPassword = '';
  displayDialog: boolean = false;
  public editedName: string = '';
  public filteredStorys: UserStory[] = [];
  public filteredEnvironments: Environment[] = [];

  // Environment detail dialog
  showEnvDetailDialog = false;
  selectedEnvironment: Environment | null = null;

  // Edit environment dialog
  showEditEnvironment = false;
  editEnvironmentName = '';
  editEnvironmentUrl = '';
  editEnvironmentUsername = '';
  editEnvironmentPassword = '';

  constructor() {
    this.projectId = +(this.route.snapshot.paramMap.get('proId') ?? '-1');
  }

  isLoading = true;

  ngOnInit(): void {
    this.projectService.getProjectById(this.projectId).subscribe(response => {
      this.currentProject = response.body ?? {id: -1, name: ''};
      this.isLoading = false;
      this.cdr.detectChanges();
    });

    this.userStoryService.getUserStories(this.projectId).subscribe({
      next: response => {
        const userStories = response.body || [];
        console.table(userStories);
        this.userStories = userStories;
        this.filteredStorys = [...this.userStories];
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to load user stories:', err.status, err.message);
      }
    });

    this.envService.getEnvironmentsByProjectId(this.projectId).subscribe(response => {
      this.environments = response.body ?? [];
      this.filteredEnvironments = [...this.environments];
      this.cdr.detectChanges();
    })
  }


  addUserStory() {
    const newUserStory: UserStory = {
      id: 0,
      name: this.newUserStoryName,
      description: this.newUserStoryDescription,
      projectID: this.projectId
    };

    this.userStoryService.createUserStory(newUserStory).subscribe({
      next: response => {
        const userStory = response.body;
        if (userStory) {
          this.userStories.push(userStory);
          this.filteredStorys = [...this.userStories];
        } else {
          console.warn('No user story returned in response:', response);
        }

        // Reset form and close dialog
        this.newUserStoryName = '';
        this.newUserStoryDescription = '';
        this.showAddUserStory = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to create user story:', err.status, err.message);
      }
    });
  }

  addEnvironment() {
    const newEnv: Environment = {
      id: 0,
      name: this.newEnvironmentName,
      url: this.newEnvironmentUrl,
      username: this.newEnvironmentUsername,
      password: this.newEnvironmentPassword,
      projectID: this.projectId
    }
    this.envService.createEnvironment(newEnv).subscribe(response => {
      const env = response.body;
      if (env) {
        this.environments.push(env);
        this.filteredEnvironments = [...this.environments];
      }
      this.newEnvironmentName = '';
      this.newEnvironmentUrl = '';
      this.newEnvironmentUsername = '';
      this.newEnvironmentPassword = '';
      this.showAddEnvironment = false;
      this.cdr.detectChanges();
    })
  }

  showEditDialog() {
    this.editedName = this.currentProject.name;
    this.displayDialog = true;
  }

  saveName() {
    this.currentProject = {...this.currentProject, name: this.editedName};

    this.projectService.renameProject(this.currentProject).subscribe(project => {
      this.currentProject = project.body ?? this.currentProject;
    })

    this.displayDialog = false;
  }

  onStorySearchChange(searchTerm: string): void {
    if (!searchTerm.trim()) {
      this.filteredStorys = [...this.userStories];
    } else {
      this.filteredStorys = this.userStories.filter(userStory =>
        userStory.name.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }
  }

  onEnvSearchChange(searchTerm: string): void {
    if (!searchTerm.trim()) {
      this.filteredEnvironments = [...this.environments];
    } else {
      this.filteredEnvironments = this.environments.filter(environment =>
        environment.name.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }
  }

  onEnvClick(env: Environment): void {
    this.selectedEnvironment = env;
    this.showEnvDetailDialog = true;
  }

  openEditEnvironment(): void {
    if (this.selectedEnvironment) {
      this.editEnvironmentName = this.selectedEnvironment.name;
      this.editEnvironmentUrl = this.selectedEnvironment.url;
      this.editEnvironmentUsername = this.selectedEnvironment.username;
      this.editEnvironmentPassword = this.selectedEnvironment.password;
      this.showEnvDetailDialog = false;
      this.showEditEnvironment = true;
    }
  }

  saveEnvironment(): void {
    if (this.selectedEnvironment) {
      const updatedEnv: Environment = {
        ...this.selectedEnvironment,
        name: this.editEnvironmentName,
        url: this.editEnvironmentUrl,
        username: this.editEnvironmentUsername,
        password: this.editEnvironmentPassword
      };

      this.envService.updateEnvironment(updatedEnv.id, updatedEnv).subscribe(response => {
        const env = response.body;
        if (env) {
          const index = this.environments.findIndex(e => e.id === env.id);
          if (index !== -1) {
            this.environments[index] = env;
            this.filteredEnvironments = [...this.environments];
          }
        }
        this.showEditEnvironment = false;
        this.selectedEnvironment = null;
        this.cdr.detectChanges();
      });
    }
  }

  deleteEnvironment(): void {
    if (this.selectedEnvironment && confirm(`Möchten Sie die Umgebung "${this.selectedEnvironment.name}" wirklich löschen?`)) {
      this.envService.deleteEnvironment(this.selectedEnvironment.id).subscribe(() => {
        this.environments = this.environments.filter(e => e.id !== this.selectedEnvironment!.id);
        this.filteredEnvironments = [...this.environments];
        this.showEnvDetailDialog = false;
        this.selectedEnvironment = null;
        this.cdr.detectChanges();
      });
    }
  }
}
