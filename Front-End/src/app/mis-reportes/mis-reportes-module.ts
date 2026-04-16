import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Lista } from './lista/lista';
import { ReporteDetail } from './reportes-detail/reportes-detail';
import { RouterModule } from '@angular/router';
import { MapaComponent } from '../shared/mapa/mapa';
import { ReportesModule } from '../reportes/reportes-module';



@NgModule({
  declarations: [
    Lista,
    ReporteDetail,
    
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReportesModule
  ]
})
export class MisReportesModule { }
