import {ComponentFixture, TestBed} from '@angular/core/testing';

import {StoryPage} from './story-page';

describe('StoryPage', () => {
  let component: StoryPage;
  let fixture: ComponentFixture<StoryPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StoryPage]
    })
      .compileComponents();

    fixture = TestBed.createComponent(StoryPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
