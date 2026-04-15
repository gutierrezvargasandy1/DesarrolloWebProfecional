import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Layout } from './layout/layout';
import { RouterOutlet } from "@angular/router";



@NgModule({
  declarations: [
    Layout
  ],
  imports: [
    CommonModule,
    RouterOutlet
]
})
export class DashboardModule { }
