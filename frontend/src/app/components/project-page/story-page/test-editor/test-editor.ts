import {Component, inject, input, OnChanges, OnInit, output, signal, SimpleChanges} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpResponse} from '@angular/common/http';
import {TableModule} from 'primeng/table';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';
import {Environment, Test, TestRun, TestStatus} from '../../../../models';
import {TestService} from '../../../../services/test.service';
import {TestRunService} from '../../../../services/test-run.service';
import {MessageService} from 'primeng/api';
import {HttpStatusCode} from '../../../../models/HttpStatusCode';
import {PollingService} from '../../../../services/polling.service';
import {generationState} from '../../../../models/generationState.interface';
import {Card} from 'primeng/card';
import {Select} from 'primeng/select';
import {Dialog} from 'primeng/dialog';
import {DrawerModule} from 'primeng/drawer';
import {MonacoEditorComponent} from '../monaco-editor/monaco-editor.component';
import Papa from 'papaparse';

interface TestStep {
  aktion: string;
  daten: string;
  erwartetesResultat: string;
}

@Component({
  selector: 'app-test-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    Card,
    Select,
    Dialog,
    DrawerModule,
    DatePipe,
    MonacoEditorComponent
  ],
  templateUrl: './test-editor.html',
  styleUrl: './test-editor.scss'
})
export class TestEditor implements OnInit, OnChanges {
  test = input.required<Test>();
  deleteItem = output<number>();

  private testService: TestService = inject(TestService);
  private testRunService: TestRunService = inject(TestRunService);
  private messageService: MessageService = inject(MessageService);
  private pollingService: PollingService = inject(PollingService);

  testSteps = signal<TestStep[]>([]);
  isSaving = signal<boolean>(false);
  isDeleting = signal<boolean>(false);
  isGenerating = signal<boolean>(false);

  environments = input.required<Environment[]>();
  selectedEnvironment = signal<Environment | null>(null);

  showCodeDialog = signal<boolean>(false);
  testCode = signal<string>('');
  isSavingCode = signal<boolean>(false);
  isLoadingCode = signal<boolean>(false);
  hasTestFile = signal<boolean>(false);

  isRunningTest = signal<boolean>(false);
  testStatus = signal<'none' | 'passed' | 'failed'>('none');
  testResult = signal<TestRun | null>(null);
  testRuns = signal<TestRun[]>([]);
  isLoadingTestRuns = signal<boolean>(false);
  showTestRunsDrawer = signal<boolean>(false);
  readonly testStatusEnum = TestStatus;

  cols = [
    {field: 'aktion', header: 'Aktion'},
    {field: 'daten', header: 'Daten'},
    {field: 'erwartetesResultat', header: 'Erwartetes Resultat'}
  ];

  ngOnInit(): void {
    this.loadTestData();
    this.isGenerating.set(this.test().generationState === generationState.IN_PROGRESS);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['test'] && !changes['test'].firstChange) {
      this.loadTestData();
    }
  }

  private loadTestData(): void {
    const steps = this.test()?.testCSV ? this.parseCSV(this.test().testCSV) : [];
    this.testSteps.set(steps);

    const env = this.environments().find(env => env.id === this.test().environmentID) || null;
    this.selectedEnvironment.set(env);

    this.checkTestFileExists();
    this.applyLatestTestRun([]);
    this.loadTestRuns();
  }

  parseCSV(csv: string): TestStep[] {
    if (!csv?.trim()) {
      return [];
    }

    const result = Papa.parse<Record<string, string>>(csv, {
      header: true,
      skipEmptyLines: true,
      dynamicTyping: false,
      delimitersToGuess: [',', '\t', '|', ';'],
      transform: (value) => value.trim()
    });

    if (result.errors.length > 0) {
      console.warn('CSV parsing errors:', result.errors);
    }

    return result.data.map(row => ({
      aktion: row['Action'] || row['Aktion'] || '',
      daten: row['Data'] || row['Daten'] || '',
      erwartetesResultat: row['Expected Result'] || row['Erwartetes Resultat'] || ''
    }));
  }

  generateCSV(): string {
    const steps = this.testSteps();
    if (!steps?.length) {
      return 'Action,Data,Expected Result';
    }

    const data = steps.map(step => ({
      'Action': step.aktion,
      'Data': step.daten,
      'Expected Result': step.erwartetesResultat
    }));

    return Papa.unparse(data, {
      header: true,
      quotes: true,
      skipEmptyLines: true
    });
  }

  addRow(): void {
    this.testSteps.update(steps => [...steps, {aktion: '', daten: '', erwartetesResultat: ''}]);
    this.updateTestCSV();
  }

  deleteRow(index: number): void {
    this.testSteps.update(steps => steps.filter((_, i) => i !== index));
    this.updateTestCSV();
  }

  onCellEdit(): void {
    this.updateTestCSV();
  }

  updateTestCSV(): void {
    this.test().testCSV = this.generateCSV();
  }

  saveTest(): void {
    if (!this.test()?.id) {
      this.messageService.add({
        severity: 'error',
        summary: 'Fehler',
        detail: 'Test-ID fehlt'
      });
      return;
    }

    this.isSaving.set(true);
    this.updateTestCSV();
    this.test().environmentID = this.selectedEnvironment()?.id || undefined;

    this.testService.updateTest(this.test().id, this.test()).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Erfolg',
          detail: 'Test erfolgreich gespeichert'
        });
      },
      error: (error) => {
        this.isSaving.set(false);
        console.error('Error saving test:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Fehler',
          detail: 'Test konnte nicht gespeichert werden'
        });
      }
    });
  }

  confirmDelete() {
    this.isDeleting.set(true);
    if (this.test) {
      this.testService.deleteTest(this.test().id).subscribe({
        next: response => {
          if (response.status === HttpStatusCode.Ok) {
            this.messageService.add({
              severity: 'success',
              summary: 'Erfolg',
              detail: 'Test erfolgreich gelöscht'
            });
            this.deleteItem.emit(this.test().id);
            this.isDeleting.set(false);
          } else {
            console.error('Failed to delete test:', response);
            this.isDeleting.set(false);
          }
        }
      });
    }
  }

  generateTest() {
    this.test().generationState = generationState.IN_PROGRESS;
    this.isGenerating.set(true);
    this.pollingService.addTest(this.test());
    this.testService.generateTest(this.test().id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'info',
          summary: 'Info',
          detail: 'Testgenerierung gestartet'
        });
      },
      error: (error) => {
        this.isGenerating.set(false);
        console.error('Error generating test:', error);
        this.pollingService.removeTest(this.test());
        this.messageService.add({
          severity: 'error',
          summary: 'Fehler',
          detail: 'Test konnte nicht generiert werden'
        });
      }
    });
  }

  checkTestFileExists(): void {
    this.testService.getTestCode(this.test().id).subscribe({
      next: response => {
        this.hasTestFile.set(response.status === HttpStatusCode.Ok && !!response.body);
      },
      error: () => {
        this.hasTestFile.set(false);
      }
    });
  }

  openCodeDialog(): void {
    this.isLoadingCode.set(true);
    this.showCodeDialog.set(true);
    this.testCode.set('');

    this.testService.getTestCode(this.test().id).subscribe({
      next: response => {
        this.isLoadingCode.set(false);
        if (response.status === HttpStatusCode.Ok && response.body) {
          this.testCode.set(response.body.code || '');
        }
      },
      error: err => {
        this.isLoadingCode.set(false);
        console.error('Error loading test code:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Fehler',
          detail: 'Test-Code konnte nicht geladen werden'
        });
        this.showCodeDialog.set(false);
      }
    });
  }

  saveTestCode(): void {
    this.isSavingCode.set(true);
    this.testService.saveTestCode(this.test().id, this.testCode()).subscribe({
      next: () => {
        this.isSavingCode.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Erfolg',
          detail: 'Test-Code erfolgreich gespeichert'
        });
        this.showCodeDialog.set(false);
      },
      error: err => {
        this.isSavingCode.set(false);
        console.error('Error saving test code:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Fehler',
          detail: 'Test-Code konnte nicht gespeichert werden'
        });
      }
    });
  }

  onCodeChange(code: string): void {
    this.testCode.set(code);
  }

  openTestRunsDrawer(): void {
    this.showTestRunsDrawer.set(true);
    if (!this.testRuns().length) {
      this.loadTestRuns();
    }
  }

  private loadTestRuns(): void {
    const currentTest = this.test();
    if (!currentTest?.id) {
      this.testRuns.set([]);
      this.applyLatestTestRun([]);
      return;
    }

    const testId = currentTest.id;

    this.isLoadingTestRuns.set(true);
    this.testRunService.getTestRuns(testId).subscribe({
      next: (response: HttpResponse<TestRun[]>) => {
        this.isLoadingTestRuns.set(false);
        const runs = response.body ?? [];
        this.testRuns.set(this.sortRuns(runs));
        this.applyLatestTestRun(this.testRuns());
      },
      error: (error: unknown) => {
        this.isLoadingTestRuns.set(false);
        console.error('Error loading test runs:', error);
        this.testRuns.set([]);
        this.applyLatestTestRun([]);
      }
    });
  }

  private mergeLatestTestRun(testRun: TestRun): void {
    this.testRuns.update(existing => {
      const filtered = existing.filter(run => run.id !== testRun.id);
      return this.sortRuns([testRun, ...filtered]);
    });
    this.applyLatestTestRun(this.testRuns());
  }

  private applyLatestTestRun(runs: TestRun[]): void {
    if (!runs?.length) {
      this.testStatus.set('none');
      this.testResult.set(null);
      return;
    }

    const latest = runs[0];
    this.testResult.set(latest);
    this.testStatus.set(this.mapStatusToDisplay(latest.status));
  }

  private mapStatusToDisplay(status: TestStatus | null | undefined): 'none' | 'passed' | 'failed' {
    if (status === TestStatus.PASSED) {
      return 'passed';
    }
    if (status === TestStatus.FAILED) {
      return 'failed';
    }
    return 'none';
  }

  private sortRuns(runs: TestRun[]): TestRun[] {
    return [...runs].sort((a, b) => this.toEpoch(b.executedAt) - this.toEpoch(a.executedAt));
  }

  private toEpoch(value?: string | null): number {
    if (!value) {
      return 0;
    }
    const parsed = new Date(value).getTime();
    return Number.isNaN(parsed) ? 0 : parsed;
  }

  private resolveSeverity(status: TestStatus): 'success' | 'error' | 'info' {
    switch (status) {
      case TestStatus.PASSED:
        return 'success';
      case TestStatus.FAILED:
        return 'error';
      default:
        return 'info';
    }
  }

  private resolveSummary(status: TestStatus): string {
    switch (status) {
      case TestStatus.PASSED:
        return 'Test erfolgreich';
      case TestStatus.FAILED:
        return 'Test fehlgeschlagen';
      case TestStatus.SKIPPED:
        return 'Test übersprungen';
      case TestStatus.PENDING:
        return 'Test in Warteschlange';
      case TestStatus.GENERATING:
        return 'Test wird generiert';
      case TestStatus.FIXING:
        return 'Test wird repariert';
      default:
        return 'Teststatus aktualisiert';
    }
  }

  runTest(): void {
    if (!this.test()?.id) {
      this.messageService.add({
        severity: 'error',
        summary: 'Fehler',
        detail: 'Test-ID fehlt'
      });
      return;
    }

    this.isRunningTest.set(true);
    this.testStatus.set('none');
    this.testResult.set(null);

    this.testService.executeTest(this.test().id).subscribe({
      next: (response: HttpResponse<TestRun>) => {
        this.isRunningTest.set(false);
        const testRun = response.body as TestRun | null;

        if (testRun) {
          this.mergeLatestTestRun(testRun);
          const severity = this.resolveSeverity(testRun.status);
          this.messageService.add({
            severity,
            summary: this.resolveSummary(testRun.status),
            detail: testRun.description
          });
        }
      },
      error: (error: unknown) => {
        this.isRunningTest.set(false);
        this.testStatus.set('failed');
        console.error('Error running test:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Fehler',
          detail: 'Test konnte nicht ausgeführt werden'
        });
      }
    });
  }
}
