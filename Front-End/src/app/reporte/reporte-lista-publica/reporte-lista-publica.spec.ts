import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReporteListaPublica } from './reporte-lista-publica';

describe('ReporteListaPublica', () => {
  let component: ReporteListaPublica;
  let fixture: ComponentFixture<ReporteListaPublica>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReporteListaPublica]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReporteListaPublica);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
