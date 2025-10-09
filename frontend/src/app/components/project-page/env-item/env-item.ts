import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Environment} from '../../../models';
import {NgClass} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-env-item',
  standalone: true,
  imports: [
    NgClass,
    RouterLink
  ],
  templateUrl: './env-item.html',
  styleUrl: './env-item.scss'
})
export class envItem {
  @Input() env!: Environment;
  @Input() icon: string = 'pi pi-globe';
  @Output() envClick = new EventEmitter<Environment>();

  onEnvClick() {
    this.envClick.emit(this.env);
  }
}
