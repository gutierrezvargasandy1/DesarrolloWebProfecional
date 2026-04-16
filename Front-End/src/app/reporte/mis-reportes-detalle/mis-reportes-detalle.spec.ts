import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MisReportesDetalle } from './mis-reportes-detalle';

describe('MisReportesDetalle', () => {
  let component: MisReportesDetalle;
  let fixture: ComponentFixture<MisReportesDetalle>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MisReportesDetalle]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MisReportesDetalle);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
