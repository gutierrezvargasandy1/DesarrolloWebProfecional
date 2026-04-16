import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreaMascota } from './crea-mascota';

describe('CreaMascota', () => {
  let component: CreaMascota;
  let fixture: ComponentFixture<CreaMascota>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreaMascota]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreaMascota);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
