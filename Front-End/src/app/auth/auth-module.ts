import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Login } from './login/login';
import { ForgotPassword } from './forgot-password/forgot-password';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ForgotPasswordCode } from './forgot-password-code/forgot-password-code';
import { ChangePassword } from './change-password/change-password';


@NgModule({
  declarations: [
    Login,
    ForgotPassword,
    ForgotPasswordCode,
    ChangePassword
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    FormsModule,

      
  ]
})
export class AuthModule { }
