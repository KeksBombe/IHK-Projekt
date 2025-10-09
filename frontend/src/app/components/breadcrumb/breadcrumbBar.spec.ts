import {ComponentFixture, TestBed} from '@angular/core/testing';

import {BreadcrumbBar} from './breadcrumbBar';

describe('Breadcrumb', () => {
  let component: BreadcrumbBar;
  let fixture: ComponentFixture<BreadcrumbBar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BreadcrumbBar]
    })
      .compileComponents();

    fixture = TestBed.createComponent(BreadcrumbBar);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
