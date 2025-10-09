import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {filter} from 'rxjs';
import {Breadcrumb} from 'primeng/breadcrumb';
import {MenuItem} from 'primeng/api';

@Component({
  selector: 'app-breadcrumbBar',
  imports: [
    Breadcrumb
  ],
  templateUrl: './breadcrumbBar.html',
  styleUrl: './breadcrumbBar.scss'
})

export class BreadcrumbBar implements OnInit {
  items: MenuItem[] = [];
  home: MenuItem = {icon: 'pi pi-home', routerLink: '/'};

  constructor(private router: Router, private activatedRoute: ActivatedRoute) {
  }

  ngOnInit() {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.items = this.createBreadcrumbs(this.activatedRoute.root);
      });
  }

  private createBreadcrumbs(route: ActivatedRoute, url: string = '', breadcrumbs: MenuItem[] = []): MenuItem[] {
    const children: ActivatedRoute[] = route.children;

    if (children.length === 0) {
      return breadcrumbs;
    }
    console.log(route);

    for (const child of children) {
      const routeConfig = child.routeConfig;

      if (!routeConfig) {
        continue;
      }

      if (routeConfig.path) {
        const routeURL = routeConfig.path;
        url += `/${routeURL}`;

        const label = routeConfig.data?.['breadcrumb'];
        if (label) {
          breadcrumbs.push({label, routerLink: url});
        }
      }

      return this.createBreadcrumbs(child, url, breadcrumbs);
    }

    return breadcrumbs;
  }

}
