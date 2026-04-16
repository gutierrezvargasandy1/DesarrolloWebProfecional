import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-layout',
  standalone: false,
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class Layout {
  menuOpen = false;

  constructor(private router: Router) {}

  // Métodos individuales para cada ruta
  irAHome(): void {
    this.router.navigate(['/dashboard/home']);
    this.menuOpen = false;
  }

  irAConversaciones(): void {
    this.router.navigate(['/dashboard/conversaciones']);
    this.menuOpen = false;
  }

  irAMascotas(): void {
    this.router.navigate(['/dashboard/mis-mascotas']);
    this.menuOpen = false;
  }

  irANuevaMascota(): void {
    this.router.navigate(['/dashboard/mis-mascotas/nueva']);
    this.menuOpen = false;
  }

  irAMisReportes(): void {
    this.router.navigate(['/dashboard/mis-reportes']);
    this.menuOpen = false;
  }

  irAAvistamientos(): void {
    this.router.navigate(['/dashboard/avistamientos']);
    this.menuOpen = false;
  }

  irANuevoReporte(): void {
    this.router.navigate(['/dashboard/crear-reporte']);
    this.menuOpen = false;
  }

  // Método genérico para saber si una ruta está activa (lo puedes usar con ngClass)
  rutaActiva(ruta: string): boolean {
    return this.router.url === `/dashboard/${ruta}` || this.router.url.startsWith(`/dashboard/${ruta}/`);
  }
}