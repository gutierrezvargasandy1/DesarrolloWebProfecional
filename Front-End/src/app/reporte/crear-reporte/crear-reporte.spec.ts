import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearReporte } from './crear-reporte';

describe('CrearReporte', () => {
  let component: CrearReporte;
  let fixture: ComponentFixture<CrearReporte>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CrearReporte]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearReporte);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
