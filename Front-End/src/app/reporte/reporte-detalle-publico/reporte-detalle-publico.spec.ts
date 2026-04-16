import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReporteDetallePublico } from './reporte-detalle-publico';

describe('ReporteDetallePublico', () => {
  let component: ReporteDetallePublico;
  let fixture: ComponentFixture<ReporteDetallePublico>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReporteDetallePublico]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReporteDetallePublico);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
