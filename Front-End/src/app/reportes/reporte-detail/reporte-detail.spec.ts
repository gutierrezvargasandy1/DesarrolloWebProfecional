import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReporteDetail } from './reporte-detail';

describe('ReporteDetail', () => {
  let component: ReporteDetail;
  let fixture: ComponentFixture<ReporteDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReporteDetail]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReporteDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
