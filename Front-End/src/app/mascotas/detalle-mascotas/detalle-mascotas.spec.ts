import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetalleMascotas } from './detalle-mascotas';

describe('DetalleMascotas', () => {
  let component: DetalleMascotas;
  let fixture: ComponentFixture<DetalleMascotas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DetalleMascotas]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DetalleMascotas);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
