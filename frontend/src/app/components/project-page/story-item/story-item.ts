import {Component, Input} from '@angular/core';
import {UserStory} from '../../../models';
import {NgClass} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-story-item',
  standalone: true,
  imports: [
    NgClass,
    RouterLink
  ],
  templateUrl: './story-item.html',
  styleUrl: './story-item.scss'
})
export class storyItem {
  @Input() story!: UserStory;
  @Input() icon: string = 'pi pi-file';
}
