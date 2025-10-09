import {
  Component,
  Input,
  OnInit,
  ChangeDetectorRef,
  inject,
  effect,
  signal,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {TableModule} from 'primeng/table';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';
import {Test} from '../../../../models';
import {TestService} from '../../../../services/test.service';
import {MessageService} from 'primeng/api';

@Component({
  selector: 'app-test-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule
  ],
  templateUrl: './test-editor.html',
  styleUrl: './test-editor.scss'
})
export class TestEditor implements OnInit, OnChanges {
  @Input() test!: Test;

  private testService = inject(TestService);
  private messageService = inject(MessageService);

  testSteps = signal<TestStep[]>([]);
  isSaving = signal<boolean>(false);

  cols = [
    {field: 'aktion', header: 'Aktion'},
    {field: 'daten', header: 'Daten'},
    {field: 'erwartetesResultat', header: 'Erwartetes Resultat'}
  ];

  ngOnInit(): void {
    this.loadTestData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['test'] && !changes['test'].firstChange) {
      this.loadTestData();
    }
  }

  private loadTestData(): void {
    const steps: TestStep[] = [];

    if (this.test && this.test.testCSV) {
      console.log('testCSV:', this.test.testCSV);
      const parsedSteps = this.parseCSV(this.test.testCSV);
      steps.push(...parsedSteps);
      console.log('Parsed testSteps:', steps);
    }

    this.testSteps.set(steps);
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
    this.test.testCSV = this.generateCSV();
  }

  saveTest(): void {
    if (!this.test || !this.test.id) {
      this.messageService.add({
        severity: 'error',
        summary: 'Fehler',
        detail: 'Test-ID fehlt'
      });
      return;
    }

    this.isSaving.set(true);
    this.updateTestCSV();

    this.testService.updateTest(this.test.id, this.test).subscribe({
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
}
