import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
  ElementRef,
  AfterViewInit
} from '@angular/core';
import * as monaco from 'monaco-editor';

@Component({
  selector: 'app-monaco-editor',
  standalone: true,
  template: `
    <div #editorContainer class="monaco-editor-container"></div>
  `,
  styleUrls: ['./monaco-editor.component.scss']
})
export class MonacoEditorComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('editorContainer', {static: true}) editorContainer!: ElementRef;
  @Input() code = '';
  @Input() language = 'typescript';
  @Input() readOnly = false;
  @Output() codeChange = new EventEmitter<string>();

  private editor?: monaco.editor.IStandaloneCodeEditor;

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.initMonaco();
  }

  ngOnDestroy(): void {
    if (this.editor) {
      this.editor.dispose();
    }
  }

  private initMonaco(): void {
    if (!this.editorContainer) {
      return;
    }

    this.editor = monaco.editor.create(this.editorContainer.nativeElement, {
      value: this.code,
      language: this.language,
      theme: 'vs-light',
      automaticLayout: true,
      readOnly: this.readOnly,
      minimap: {
        enabled: true
      },
      fontSize: 14,
      lineNumbers: 'on',
      scrollBeyondLastLine: false,
      wordWrap: 'on',
      wrappingIndent: 'indent'
    });

    this.editor.onDidChangeModelContent(() => {
      if (this.editor) {
        this.codeChange.emit(this.editor.getValue());
      }
    });
  }

  public setValue(value: string): void {
    if (this.editor) {
      this.editor.setValue(value);
    }
  }

  public getValue(): string {
    return this.editor?.getValue() || '';
  }
}
