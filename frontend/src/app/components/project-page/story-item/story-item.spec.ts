import {ComponentFixture, TestBed} from '@angular/core/testing';

import {storyItem} from './story-item';

describe('storyItem', () => {
  let component: storyItem;
  let fixture: ComponentFixture<storyItem>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [storyItem]
    })
      .compileComponents();

    fixture = TestBed.createComponent(storyItem);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
