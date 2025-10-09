import {ComponentFixture, TestBed} from '@angular/core/testing';

import {envItem} from './env-item';

describe('envItem', () => {
  let component: envItem;
  let fixture: ComponentFixture<envItem>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [envItem]
    })
      .compileComponents();

    fixture = TestBed.createComponent(envItem);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
