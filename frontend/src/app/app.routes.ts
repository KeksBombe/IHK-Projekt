import {Routes} from '@angular/router';
import {LandingPage} from './components/landing-page/landing-page';
import {ProjectPage} from './components/project-page/project-page';
import {StoryPage} from './components/project-page/story-page/story-page';

export const routes: Routes = [
  {
    path: '',
    component: LandingPage,
    title: 'Overview',
    data: {breadcrumb: 'Overview'}
  },
  {
    path: 'project/:proId',
    component: ProjectPage,
    title: 'Project Details',
    data: {breadcrumb: 'Project'}
  },
  {
    path: 'project/:proId/story/:storyId',
    component: StoryPage,
    title: 'Userstory Details',
    data: {breadcrumb: 'UserStory'}
  }
];
