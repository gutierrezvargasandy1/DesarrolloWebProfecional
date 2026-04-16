import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Login } from './auth/login/login';
import { Layout } from './dashboard/layout/layout';
import { ForgotPassword } from './auth/forgot-password/forgot-password';
import { ForgotPasswordCode } from './auth/forgot-password-code/forgot-password-code';
import { ChangePassword } from './auth/change-password/change-password';
import { Home } from './dashboard/home/home';
import { ReporteDetallePublico } from './reporte/reporte-detalle-publico/reporte-detalle-publico';
import { MisReportesDetalle } from './reporte/mis-reportes-detalle/mis-reportes-detalle';
import { MisReportes } from './reporte/mis-reportes.ts/mis-reportes';
import { CrearReporte } from './reporte/crear-reporte/crear-reporte';

import { MisMascotas } from './mascotas/mis-mascotas/mis-mascotas';
import { CreaMascota } from './mascotas/crea-mascota/crea-mascota';
import { DetalleMascotas } from './mascotas/detalle-mascotas/detalle-mascotas';

const routes: Routes = [

  // ================= DASHBOARD =================
  {
    path: 'dashboard',
    component: Layout,
    children: [

      { path: 'home', component: Home },
      { path: 'reporte/:id', component: ReporteDetallePublico },

      // Rutas del user en sesión (reportes)
      { path: 'mis-reportes', component: MisReportes },
      { path: 'mis-reportes/:id', component: MisReportesDetalle },
      { path: 'crear-reporte', component: CrearReporte },

      // Rutas de mascotas
      { path: 'mis-mascotas', component: MisMascotas },
      { path: 'mis-mascotas/nueva', component: CreaMascota },
      { path: 'mis-mascotas/:id', component: DetalleMascotas },

      // Ruta por defecto del dashboard
      { path: '', redirectTo: 'home', pathMatch: 'full' },
    ],
  },

  // ================= AUTH =================
  { path: 'auth/login', component: Login },
  { path: 'auth/forget-password', component: ForgotPassword },
  { path: 'auth/forgot-password-code', component: ForgotPasswordCode },
  { path: 'auth/change-password', component: ChangePassword },

  // ================= ROOT =================
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }