import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ProjectSearch} from './project-search';

describe('ProjectSearch', () => {
  let component: ProjectSearch;
  let fixture: ComponentFixture<ProjectSearch>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectSearch]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ProjectSearch);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
