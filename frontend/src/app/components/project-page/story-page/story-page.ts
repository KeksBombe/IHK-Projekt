import {ChangeDetectorRef, Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterOutlet} from '@angular/router';
import {Card} from 'primeng/card';
import {Button} from 'primeng/button';
import {DataView} from 'primeng/dataview';
import {MessageService, PrimeTemplate} from 'primeng/api';
import {Splitter} from 'primeng/splitter';
import {Environment, Test, UserStory} from '../../../models';
import {UserStoryService} from '../../../services/user-story.service';
import {TestService} from '../../../services/test.service';
import {TestItem} from './test-item/test-item';
import {ProjectSearch} from '../../landing-page/project-search/project-search';
import {Dialog} from 'primeng/dialog';
import {InputText} from 'primeng/inputtext';
import {FormsModule} from '@angular/forms';
import {TestEditor} from './test-editor/test-editor';
import {HttpStatusCode} from '../../../models/HttpStatusCode';
import {Toast} from 'primeng/toast';
import {generationState} from '../../../models/generationState.interface';
import {EnvironmentService} from '../../../services/environment.service';
import {Select} from 'primeng/select';

@Component({
  selector: 'app-story-page',
  standalone: true,
  imports: [
    RouterOutlet,
    Card,
    Button,
    DataView,
    PrimeTemplate,
    Splitter,
    TestItem,
    ProjectSearch,
    Dialog,
    InputText,
    FormsModule,
    TestEditor,
    Toast,
    Select
  ],
  templateUrl: './story-page.html',
  styleUrl: './story-page.scss'
})
class StoryPage implements OnInit {
  readonly projectId: number;
  readonly storyId: number;
  private route = inject(ActivatedRoute);
  private userStoryService = inject(UserStoryService);
  private testService = inject(TestService);
  private envService = inject(EnvironmentService);
  private cdr = inject(ChangeDetectorRef);
  private messageService = inject(MessageService);


  currentStory: UserStory = {id: -1, name: '', description: '', projectID: -1};
  tests: Test[] = [];
  filteredTests: Test[] = [];
  selectedTest: Test | null = null;

  environments: Environment[] = [];
  selectedEnvironment: Environment | null = null;

  isLoading = true;
  displayDialog = false;
  editedName = '';
  editedDescription = '';

  splitterSizes: number[] = [100, 0];

  showAddTest = false;
  newTestName = '';
  newTestDescription = '';

  constructor() {
    this.projectId = +(this.route.snapshot.paramMap.get('proId') ?? '-1');
    this.storyId = +(this.route.snapshot.paramMap.get('storyId') ?? '-1');
  }

  ngOnInit(): void {
    this.userStoryService.getUserStoryById(this.storyId).subscribe({
      next: response => {
        if (response.body) {
          this.currentStory = response.body;
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Error loading user stories:', err.status, err.message);
        this.isLoading = false;
      }
    });

    this.envService.getEnvironmentsByProjectId(this.projectId).subscribe({
      next: response => {
        this.environments = response.body ?? [];
        console.table(this.environments);
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Error loading environments:', err.status, err.message);
      }
    })

    this.testService.getTestsByUserStoryId(this.storyId).subscribe(response => {
      this.tests = response.body ?? [];
      this.filteredTests = [...this.tests];
      this.cdr.detectChanges();
    });
  }

  onTestClick(test: Test): void {
    this.selectedTest = test;
    this.splitterSizes = [30, 70];
    this.cdr.detectChanges();
  }

  onTestSearchChange(searchTerm: string): void {
    if (!searchTerm.trim()) {
      this.filteredTests = [...this.tests];
    } else {
      this.filteredTests = this.tests.filter(test =>
        test.name.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }
  }

  showEditDialog(): void {
    this.editedName = this.currentStory.name;
    this.editedDescription = this.currentStory.description;
    this.displayDialog = true;
  }

  saveName(): void {
    this.currentStory = {
      ...this.currentStory,
      name: this.editedName,
      description: this.editedDescription
    };

    this.userStoryService.updateUserStory(this.currentStory).subscribe({
      next: response => {
        if (response.status === HttpStatusCode.Ok && response.body) {
          this.currentStory = response.body;
          this.displayDialog = false;
          this.cdr.detectChanges();
        } else {
          console.error('Failed to update user story:', response);
        }
      }
    });
  }

  addTest(): void {
    const newTest: Test = {
      id: 0,
      name: this.newTestName,
      description: this.newTestDescription,
      testCSV: '',
      environmentID: this.selectedEnvironment?.id || undefined,
      storyID: this.storyId,
      generationState: generationState.NOT_STARTED
    };
    console.table(newTest);
    this.testService.createTest(newTest).subscribe({
      next: response => {
        if (response.status === HttpStatusCode.Created && response.body) {
          const newTest = response.body;
          this.tests.push(newTest);
          this.selectedTest = newTest;
          this.filteredTests = [...this.tests];
          this.newTestName = '';
          this.newTestDescription = '';
          this.selectedEnvironment = null;
          this.showAddTest = false;
          this.splitterSizes = [30, 70];
          this.messageService.add({
            severity: 'success',
            summary: 'Erfolg',
            detail: 'Test erfolgreich erstellt'
          });
          this.cdr.detectChanges();
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Fehler',
            detail: 'Bei der Erstellung des Tests ist ein Fehler aufgetreten'
          });
          console.error('Failed to create test:', response);
        }
      }
    });
  }

  closeSplitter() {
    this.selectedTest = null;
    this.splitterSizes = [100, 0];
  }

  deleteItem(id: number) {
    this.tests = this.tests.filter(t => t.id !== id);
    this.filteredTests = [...this.tests];
    this.closeSplitter();
  }
}

export default StoryPage
