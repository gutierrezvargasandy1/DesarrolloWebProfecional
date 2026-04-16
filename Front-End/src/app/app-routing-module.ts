import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Layout } from './dashboard/layout/layout';
import { ForgotPassword } from './auth/forgot-password/forgot-password';
import { ForgotPasswordCode } from './auth/forgot-password-code/forgot-password-code';
import { ChangePassword } from './auth/change-password/change-password';
import { Lista } from './reportes/lista/lista';
import { MisReportesLista } from './mis-reportes/lista/lista';

import { ReporteDetail } from './reportes/reporte-detail/reporte-detail';

const routes: Routes = [

  //Rutas Modulo Principal 
  {
    path: 'dashboard',
    component: Layout,
    children: [
       
      //Reportes
      {path: 'reporte/lista',component:Lista},
      {path: 'reporte/:id', component: ReporteDetail},

      //Mis Reportes 
      {path:}

      // Ruta Por defecto
      { path: '', redirectTo: 'reporte/lista', pathMatch: 'full' }
    ]
  },
   
  // Rutas Modulo Auth
  {path: 'auth/login', component: Login},
  {path: 'auth/forget-password', component: ForgotPassword},
  {path: 'auth/forgot-password-code', component: ForgotPasswordCode},
  {path: 'auth/change-password', component: ChangePassword},

  // Ruta por defecto
  //{path: '', redirectTo : 'auth/login' , pathMatch:'full'},
  {path: '', redirectTo : 'dashboard' , pathMatch:'full'},

   

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
