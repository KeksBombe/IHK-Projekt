import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Test} from '../../../../models';
import {NgClass} from '@angular/common';

@Component({
  selector: 'app-test-item',
  standalone: true,
  imports: [
    NgClass
  ],
  templateUrl: './test-item.html',
  styleUrl: './test-item.scss'
})
export class TestItem {
  @Input() test!: Test;
  @Input() icon: string = 'pi pi-file-check';
  @Input() isSelected: boolean = false;
  @Output() testClick = new EventEmitter<Test>();

  onClick(): void {
    this.testClick.emit(this.test);
  }
}
