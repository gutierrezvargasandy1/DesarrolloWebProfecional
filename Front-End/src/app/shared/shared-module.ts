// shared-module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapaComponent } from './components/mapa/mapa';

@NgModule({
  declarations: [MapaComponent],
  imports: [CommonModule],
  exports: [MapaComponent]   
})
export class SharedModule {}