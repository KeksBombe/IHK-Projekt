import {Component, Input} from '@angular/core';
import {Project} from '../../../models';
import {NgClass} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-project-item',
  standalone: true,
  imports: [
    NgClass,
    RouterLink
  ],
  templateUrl: './project-item.html',
  styleUrl: './project-item.scss'
})
export class ProjectItem {
  @Input() project!: Project;
  @Input() icon: string = 'pi pi-folder';
}
