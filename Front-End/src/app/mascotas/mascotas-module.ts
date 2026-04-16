import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { CreaMascota } from './crea-mascota/crea-mascota';
import { MisMascotas } from './mis-mascotas/mis-mascotas';
import { DetalleMascotas } from './detalle-mascotas/detalle-mascotas';
import { SharedModule } from '../shared/shared-module';

@NgModule({
  declarations: [
    CreaMascota,
    MisMascotas,
    DetalleMascotas,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    SharedModule
  ],
  exports: [
    CreaMascota,
    MisMascotas,
    DetalleMascotas,
  ],
})
export class MascotasModule { }