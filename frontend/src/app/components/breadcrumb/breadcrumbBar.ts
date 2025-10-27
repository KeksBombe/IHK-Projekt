import {Component, inject, OnDestroy, OnInit, ChangeDetectorRef} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {filter, forkJoin, Subscription, timeout} from 'rxjs';
import {Breadcrumb} from 'primeng/breadcrumb';
import {MenuItem} from 'primeng/api';
import {ProjectService} from '../../services/project.service';
import {UserStoryService} from '../../services/user-story.service';

@Component({
  selector: 'app-breadcrumbBar',
  imports: [
    Breadcrumb
  ],
  templateUrl: './breadcrumbBar.html',
  styleUrl: './breadcrumbBar.scss'
})
export class BreadcrumbBar implements OnInit, OnDestroy {
  items: MenuItem[] = [];
  home: MenuItem = {
    icon: 'pi pi-home',
    url: '/'
  };

  private projectService = inject(ProjectService);
  private userStoryService = inject(UserStoryService);
  private cdr = inject(ChangeDetectorRef);
  private routerSubscription?: Subscription;
  private dataSubscription?: Subscription;

  constructor(private router: Router, private activatedRoute: ActivatedRoute) {
  }

  ngOnInit() {
    this.updateBreadcrumbs();

    this.routerSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateBreadcrumbs();
      });
  }

  ngOnDestroy() {
    this.routerSubscription?.unsubscribe();
    this.dataSubscription?.unsubscribe();
  }

  private updateBreadcrumbs(): void {
    this.dataSubscription?.unsubscribe();

    const params = this.getAllRouteParams(this.activatedRoute.root);
    const projectId = params['projectId'] ? +params['projectId'] : (params['proId'] ? +params['proId'] : null);
    const storyId = params['storyId'] ? +params['storyId'] : null;

    if (projectId && storyId) {
      this.items = [
        {label: 'Loading...', url: `/project/${projectId}`},
        {label: 'Loading...', url: `/project/${projectId}/story/${storyId}`}
      ];

      this.dataSubscription = forkJoin({
        project: this.projectService.getProjectById(projectId),
        story: this.userStoryService.getUserStoryById(storyId)
      }).subscribe({
        next: ({project, story}) => {
          this.items = [
            {
              label: project.body?.name || 'Project',
              url: `/project/${projectId}`
            },
            {
              label: story.body?.name || 'Story',
              url: `/project/${projectId}/story/${storyId}`
            }
          ];
          this.cdr.detectChanges();
        },
        error: err => {
          console.error('Error loading breadcrumb data:', err);
          this.items = [
            {label: 'Project', url: `/project/${projectId}`},
            {label: 'Story', url: `/project/${projectId}/story/${storyId}`}
          ];
        }
      });
    } else if (projectId) {
      this.items = [{label: 'Loading...', url: `/project/${projectId}`}];

      this.dataSubscription = this.projectService.getProjectById(projectId)
        .pipe(timeout(5000))
        .subscribe({
          next: response => {
            this.items = [
              {
                label: response.body?.name || 'Project',
                url: `/project/${projectId}`
              }
            ];

            this.cdr.detectChanges();
          },
          error: err => {
            this.items = [{label: 'Project', url: `/project/${projectId}`}];
          }
        });
    } else {
      this.items = [];
    }
  }

  private getAllRouteParams(route: ActivatedRoute): any {
    let params = {};

    let currentRoute: ActivatedRoute | null = route;
    while (currentRoute) {
      params = {...params, ...currentRoute.snapshot.params};
      currentRoute = currentRoute.firstChild;
    }

    return params;
  }
}
