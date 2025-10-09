import {Component, Input} from '@angular/core';
import {Project} from '../../../models';
import {ProjectItem} from '../project-item/project-item';
import {NgForOf} from '@angular/common';

@Component({
  selector: 'app-recent-projects',
  imports: [
    ProjectItem,
    NgForOf
  ],
  templateUrl: './recent-projects.html',
  styleUrl: './recent-projects.scss'
})
export class RecentProjects {

  @Input() projects: Project[] = [];

}
