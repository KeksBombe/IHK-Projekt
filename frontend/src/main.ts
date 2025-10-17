import {bootstrapApplication} from '@angular/platform-browser';
import {appConfig} from './app/app.config';
import {App} from './app/app';

// Configure Monaco Editor
(window as any).MonacoEnvironment = {
  getWorkerUrl: function (workerId: string, label: string) {
    if (label === 'json') {
      return './assets/monaco-editor/vs/language/json/json.worker.js';
    }
    if (label === 'css' || label === 'scss' || label === 'less') {
      return './assets/monaco-editor/vs/language/css/css.worker.js';
    }
    if (label === 'html' || label === 'handlebars' || label === 'razor') {
      return './assets/monaco-editor/vs/language/html/html.worker.js';
    }
    if (label === 'typescript' || label === 'javascript') {
      return './assets/monaco-editor/vs/language/typescript/ts.worker.js';
    }
    return './assets/monaco-editor/vs/editor/editor.worker.js';
  }
};

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
