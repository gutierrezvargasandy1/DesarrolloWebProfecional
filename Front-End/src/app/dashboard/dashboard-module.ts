// dashboard-module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { ReporteModule } from '../reporte/reporte-module';
import { Layout } from './layout/layout';
import { Home } from './home/home';

@NgModule({
  declarations: [Layout, Home],
  imports: [
    CommonModule,
    RouterOutlet,
    ReporteModule   // ✅ Solo importa, no declara componentes de otros módulos
  ]
})
export class DashboardModule { }