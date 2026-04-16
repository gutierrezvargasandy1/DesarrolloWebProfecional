import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Lista } from './lista/lista';
import { ReporteDetail } from './reporte-detail/reporte-detail';
import { MapaComponent } from '../shared/mapa/mapa';
import { RouterModule } from '@angular/router';



@NgModule({
  declarations: [
    Lista,
    ReporteDetail,
    MapaComponent
  ],
  exports: [MapaComponent],
  imports: [
    CommonModule,
    RouterModule      
  ]
})
export class ReportesModule { }
