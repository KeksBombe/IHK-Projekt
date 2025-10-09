import {Component, Output, EventEmitter, Input} from '@angular/core';
import {InputText} from 'primeng/inputtext';
import {IconField} from 'primeng/iconfield';
import {InputIcon} from 'primeng/inputicon';

@Component({
  selector: 'app-project-search',
  imports: [
    InputText,
    IconField,
    InputIcon
  ],
  templateUrl: './project-search.html',
  styleUrl: './project-search.scss'
})
export class ProjectSearch {
  @Output() searchChange = new EventEmitter<string>();
  @Input() placeholder: string = 'Projekt suchen...';

  onSearchInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchChange.emit(target.value);
  }
}
