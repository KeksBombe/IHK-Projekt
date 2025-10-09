import {Component, Input} from '@angular/core';
import {Project, UserStory, Test} from '../../../models';
import {ProjectItem} from '../project-item/project-item';

@Component({
  selector: 'app-continue-section',
  imports: [
    ProjectItem
  ],
  templateUrl: './continue-section.html',
  styleUrl: './continue-section.scss'
})
export class ContinueSection {

  @Input() item!: Project | UserStory | Test;

}
