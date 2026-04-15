import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Layout } from './dashboard/layout/layout';
import { ForgotPassword } from './auth/forgot-password/forgot-password';

const routes: Routes = [

  //Rutas Modulo Principal 
  {
    path: 'dashboard',
    component: Layout,
    children: [

      // Ruta Por defecto
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
   
  // Rutas Modulo Auth
  {path: 'auth/login', component: Login},
  {path: 'auth/forget-password', component: ForgotPassword},
  {path: 'auth/forgot-password-code', component: ForgotPassword},
  {path: 'auth/change-password', component: ForgotPassword},

  // Ruta por defecto
  {path: '', redirectTo : 'auth/login' , pathMatch:'full'},
   

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
