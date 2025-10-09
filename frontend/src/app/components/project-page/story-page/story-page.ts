import {ChangeDetectorRef, Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterOutlet} from '@angular/router';
import {Card} from 'primeng/card';
import {Button} from 'primeng/button';
import {DataView} from 'primeng/dataview';
import {PrimeTemplate} from 'primeng/api';
import {Splitter} from 'primeng/splitter';
import {Test, UserStory} from '../../../models';
import {UserStoryService} from '../../../services/user-story.service';
import {TestService} from '../../../services/test.service';
import {TestItem} from './test-item/test-item';
import {ProjectSearch} from '../../landing-page/project-search/project-search';
import {JsonPipe} from '@angular/common';
import {Dialog} from 'primeng/dialog';
import {InputText} from 'primeng/inputtext';
import {FormsModule} from '@angular/forms';

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
    JsonPipe,
    Dialog,
    InputText,
    FormsModule
  ],
  templateUrl: './story-page.html',
  styleUrl: './story-page.scss'
})
export class StoryPage implements OnInit {
  readonly projectId: number;
  readonly storyId: number;
  private route = inject(ActivatedRoute);
  private userStoryService = inject(UserStoryService);
  private testService = inject(TestService);
  private cdr = inject(ChangeDetectorRef);

  currentStory: UserStory = {id: -1, name: '', description: '', projectID: -1};
  tests: Test[] = [];
  filteredTests: Test[] = [];
  selectedTest: Test | null = null;

  isLoading = true;
  displayDialog = false;
  editedName = '';
  editedDescription = '';

  // Splitter sizes
  splitterSizes: number[] = [100, 0];

  // Dialog für neuen Test
  showAddTest = false;
  newTestName = '';
  newTestDescription = '';

  constructor() {
    this.projectId = +(this.route.snapshot.paramMap.get('proId') ?? '-1');
    this.storyId = +(this.route.snapshot.paramMap.get('storyId') ?? '-1');
  }

  ngOnInit(): void {
    // Lade User Story Details
    this.userStoryService.getUserStories(this.projectId).subscribe(stories => {
      const story = stories.find(s => s.id === this.storyId);
      if (story) {
        this.currentStory = story;
      }
      this.isLoading = false;
      this.cdr.detectChanges();
    });

    this.testService.getTestsByUserStoryId(this.storyId).subscribe(tests => {
      this.tests = tests;
      this.filteredTests = [...this.tests];
      this.cdr.detectChanges();
    });
  }

  onTestClick(test: Test): void {
    this.selectedTest = test;
    // Animiere den Splitter, um die rechte Seite zu öffnen
    this.splitterSizes = [50, 50];
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
    // Update current story
    this.currentStory = {
      ...this.currentStory,
      name: this.editedName,
      description: this.editedDescription
    };

    // TODO: Service-Call zum Aktualisieren der User Story
    // this.userStoryService.updateUserStory(this.currentStory).subscribe(...);

    this.displayDialog = false;
  }

  addTest(): void {
    const newTest: Test = {
      id: 0,
      name: this.newTestName,
      description: this.newTestDescription,
      testJson: '',
      environmentId: 0,
      projectId: this.projectId
    };

    this.testService.createTest(newTest).subscribe(test => {
      this.tests.push(test);
      this.filteredTests = [...this.tests];
      this.newTestName = '';
      this.newTestDescription = '';
      this.showAddTest = false;
      this.cdr.detectChanges();
    });
  }
}
