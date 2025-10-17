import {Component, computed, inject, input, OnChanges, OnInit, output, signal, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {TableModule} from 'primeng/table';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';
import {Environment, Test} from '../../../../models';
import {TestService} from '../../../../services/test.service';
import {MessageService} from 'primeng/api';
import {HttpStatusCode} from '../../../../models/HttpStatusCode';
import {PollingService} from '../../../../services/polling.service';
import {generationState} from '../../../../models/generationState.interface';
import {Card} from 'primeng/card';
import {Select} from 'primeng/select';
import {Dialog} from 'primeng/dialog';
import {MonacoEditorComponent} from '../monaco-editor/monaco-editor.component';

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
    MonacoEditorComponent
  ],
  templateUrl: './test-editor.html',
  styleUrl: './test-editor.scss'
})
export class TestEditor implements OnInit, OnChanges {
  test = input.required<Test>();
  deleteItem = output<number>();

  private testService = inject(TestService);
  private messageService = inject(MessageService);
  private pollingService = inject(PollingService);

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
  testResult = signal<any>(null);

  cols = [
    {field: 'aktion', header: 'Aktion'},
    {field: 'daten', header: 'Daten'},
    {field: 'erwartetesResultat', header: 'Erwartetes Resultat'}
  ];

  ngOnInit(): void {
    this.loadTestData();
    this.isGenerating.set(this.test().generationState === generationState.IN_PROGRESS);
    this.checkTestFileExists();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['test'] && !changes['test'].firstChange) {
      this.loadTestData();
    }
  }

  private loadTestData(): void {
    const steps: TestStep[] = [];

    if (this.test && this.test().testCSV) {
      console.log('testCSV:', this.test().testCSV);
      const parsedSteps = this.parseCSV(this.test().testCSV);
      steps.push(...parsedSteps);
      console.log('Parsed testSteps:', steps);
    }

    this.testSteps.set(steps);
    const env = this.environments().find(env => env.id === this.test().environmentID) || null;
    this.selectedEnvironment.set(env);
    this.checkTestFileExists();
  }

  parseCSV(csv: string): TestStep[] {
    if (!csv || csv.trim() === '') {
      console.log('CSV ist leer');
      return [];
    }

    console.log('Original CSV:', csv);

    // Behandle sowohl echte Newlines als auch escaped Newlines (\\n)
    // Das ist wichtig, weil JSON oft \\n als String enthält
    const normalizedCsv = csv.replace(/\\n/g, '\n');
    console.log('Normalized CSV:', normalizedCsv);

    const lines = normalizedCsv.split('\n').filter(line => line.trim() !== '');
    console.log('Lines:', lines);

    const steps: TestStep[] = [];

    // Skip header line
    for (let i = 1; i < lines.length; i++) {
      console.log(`Parsing line ${i}:`, lines[i]);
      const values = this.parseCSVLine(lines[i]);
      console.log(`Parsed values:`, values);

      if (values.length >= 3) {
        steps.push({
          aktion: values[0],
          daten: values[1],
          erwartetesResultat: values[2]
        });
      }
    }

    console.log('Final testSteps:', steps);
    return steps;
  }

  parseCSVLine(line: string): string[] {
    const result: string[] = [];
    let current = '';
    let inQuotes = false;

    for (let i = 0; i < line.length; i++) {
      const char = line[i];

      if (char === '"') {
        inQuotes = !inQuotes;
      } else if (char === ',' && !inQuotes) {
        result.push(current.trim());
        current = '';
      } else {
        current += char;
      }
    }

    result.push(current.trim());
    return result;
  }

  generateCSV(): string {
    // Sicherheitsprüfung
    const steps = this.testSteps();
    if (!steps || !Array.isArray(steps) || steps.length === 0) {
      return 'Aktion,Daten,Erwartetes Resultat';
    }

    const header = 'Aktion,Daten,Erwartetes Resultat';
    const rows = steps.map(step => {
      const aktion = `"${(step.aktion || '').replace(/"/g, '""')}"`;
      const daten = `"${(step.daten || '').replace(/"/g, '""')}"`;
      const resultat = `"${(step.erwartetesResultat || '').replace(/"/g, '""')}"`;
      return `${aktion},${daten},${resultat}`;
    });

    // Verwende echte Newlines beim Generieren
    return [header, ...rows].join('\n');
  }

  addRow(): void {
    // Hole aktuelle testSteps und füge neue Zeile hinzu
    const currentSteps = [...this.testSteps()];
    currentSteps.push({
      aktion: '',
      daten: '',
      erwartetesResultat: ''
    });
    this.testSteps.set(currentSteps);
    this.updateTestCSV();
  }

  deleteRow(index: number): void {
    const currentSteps = [...this.testSteps()];
    if (currentSteps.length > index) {
      currentSteps.splice(index, 1);
      this.testSteps.set(currentSteps);
      this.updateTestCSV();
    }
  }

  onCellEdit(): void {
    this.updateTestCSV();
  }

  updateTestCSV(): void {
    this.test().testCSV = this.generateCSV();
  }

  saveTest(): void {
    if (!this.test || !this.test().id) {
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

  runTest(): void {
    if (!this.test || !this.test().id) {
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
      next: (response) => {
        this.isRunningTest.set(false);
        const testRun = response.body;

        if (testRun) {
          if (testRun.status === 'PASSED') {
            this.testStatus.set('passed');
            this.messageService.add({
              severity: 'success',
              summary: 'Test erfolgreich'
            });
          } else if (testRun.status === 'FAILED') {
            this.testStatus.set('failed');
            this.messageService.add({
              severity: 'error',
              summary: 'Test fehlgeschlagen'
            });
          } else {
            this.testStatus.set('none');
            this.messageService.add({
              severity: 'info',
              summary: 'Test Status: ' + testRun.status
            });
          }
        }
      },
      error: (error) => {
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
