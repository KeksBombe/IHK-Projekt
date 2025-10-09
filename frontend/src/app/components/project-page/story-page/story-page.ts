import {ChangeDetectorRef, Component, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute, RouterOutlet} from '@angular/router';
import {Card} from 'primeng/card';
import {Button} from 'primeng/button';
import {DataView} from 'primeng/dataview';
import {MessageService, PrimeTemplate} from 'primeng/api';
import {Splitter} from 'primeng/splitter';
import {Test, UserStory} from '../../../models';
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
    Toast
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
  private cdr = inject(ChangeDetectorRef);
  private messageService = inject(MessageService);

  currentStory: UserStory = {id: -1, name: '', description: '', projectID: -1};
  tests: Test[] = [];
  filteredTests: Test[] = [];
  selectedTest: Test | null = null;

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
    // Lade User Story Details
    this.userStoryService.getUserStories(this.projectId).subscribe({
      next: response => {
        const stories = response.body || [];
        const story = stories.find(s => s.id === this.storyId);
        if (story) {
          this.currentStory = story;
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Error loading user stories:', err.status, err.message);
        this.isLoading = false;
      }
    });


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
      environmentID: 0,
      storyID: this.storyId
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
          this.showAddTest = false;
          this.splitterSizes = [30, 70];
          this.cdr.detectChanges();
        } else {
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
