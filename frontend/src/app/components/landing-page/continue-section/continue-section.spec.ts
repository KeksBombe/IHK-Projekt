import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ContinueSection} from './continue-section';

describe('ContinueSection', () => {
  let component: ContinueSection;
  let fixture: ComponentFixture<ContinueSection>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContinueSection]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ContinueSection);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
