// reporte-module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared-module';

import { ReporteListaPublica } from './reporte-lista-publica/reporte-lista-publica';
import { ReporteDetallePublico } from './reporte-detalle-publico/reporte-detalle-publico';
import { MisReportesDetalle } from './mis-reportes-detalle/mis-reportes-detalle';
import { MisReportes } from './mis-reportes.ts/mis-reportes';
import { CrearReporte } from './crear-reporte/crear-reporte';

@NgModule({
  declarations: [
    ReporteListaPublica,
    ReporteDetallePublico,
    MisReportes,
    MisReportesDetalle,
    CrearReporte,
    // ❌ NO incluyas SharedModule aquí
    // ❌ NO incluyas MapaComponent (ya está en SharedModule)
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    SharedModule   // ✅ Importas SharedModule para usar MapaComponent
  ],
  exports: [
    ReporteListaPublica,
    ReporteDetallePublico,
    MisReportes,
    MisReportesDetalle
  ]
})
export class ReporteModule { }