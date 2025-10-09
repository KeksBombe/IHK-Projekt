import {ChangeDetectorRef, Component, inject, OnInit} from '@angular/core';
import {ProjectSearch} from './project-search/project-search';
import {ProjectItem} from './project-item/project-item';
import {ContinueSection} from './continue-section/continue-section';
import {RecentProjects} from './recent-projects/recent-projects';
import {Project, Test, UserStory} from '../../models';
import {ProjectService} from '../../services/project.service';
import {Button} from 'primeng/button';
import {DataView} from 'primeng/dataview';
import {Dialog} from 'primeng/dialog';
import {InputText} from 'primeng/inputtext';
import {FormsModule} from '@angular/forms';
import {MessageService} from 'primeng/api';
import {Toast} from 'primeng/toast';

@Component({
  selector: 'app-landing-page',
  imports: [
    ProjectSearch,
    ProjectItem,
    ContinueSection,
    RecentProjects,
    Button,
    DataView,
    Dialog,
    InputText,
    FormsModule,
    Toast
  ],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.scss',
  providers: [MessageService]
})

export class LandingPage implements OnInit {

  projects: Project[] = [];
  filteredProjects: Project[] = [];
  private projectService = inject(ProjectService);
  public recentProjects: Project[] = [];
  public continueItem: Project | Test | UserStory = {id: 0, name: "Placeholder"};
  visible: boolean = false;
  projectName: String = '';

  constructor(private messageService: MessageService, private cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.projectService.getAllProjects().subscribe(response => {
      this.projects = response.body ?? [];
      console.log('Projects loaded:', this.projects);
      this.updateFilteredProjects();
      this.continueItem = this.projects[0];
      this.recentProjects = this.projects.slice(0, 3);
      this.cdr.detectChanges();
    });
  }

  updateFilteredProjects(): void {
    this.filteredProjects = [...this.projects];
  }

  onSearchChange(searchTerm: string): void {
    if (!searchTerm.trim()) {
      this.filteredProjects = [...this.projects];
    } else {
      this.filteredProjects = this.projects.filter(project =>
        project.name.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }
  }

  showDialog() {
    this.visible = true;
  }


  closeDialog() {
    this.visible = false;
  }

  saveProject() {
    const name = this.projectName.trim();
    if (name) {
      this.closeDialog();
      this.projectService.createProject(name).subscribe({
        next: (response) => {
          const status = response.status;

          if (status === 201) {
            const newProject: Project | null = response.body;
            if (newProject) {
              this.projects.push(newProject);
              this.cdr.detectChanges();
              this.updateFilteredProjects();
              this.messageService.add({
                severity: 'success',
                summary: 'Success',
                detail: 'Projekt "' + this.projectName + '" wurde erstellt'
              });
              this.projectName = '';
            }
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Fehler beim Erstellen des Projekts'
            });
          }
        }
      })
    } else {
      console.error('Project name cannot be empty');
    }
  }

  isInvalid() {
    return !this.projectName || this.projectName.trim() === '';

  }
}
