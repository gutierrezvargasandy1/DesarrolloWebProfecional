import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReporteService } from '../../core/services/reportes/reporte.service';
import { ReporteConMascota } from '../../core/services/reportes/model/reporte';
import { UrlHelper } from '../../core/utils/url.helper';

@Component({
  selector: 'app-mis-reportes',
  standalone: false,
  templateUrl: './mis-reportes.html',
  styleUrls: ['./mis-reportes.css']
})
export class MisReportes implements OnInit {
  reportes: ReporteConMascota[] = [];
  reportesFiltrados: ReporteConMascota[] = [];
  busqueda = '';
  loading = true;
  error = false;

  // Exponemos el helper para usarlo en el template
  urlHelper = UrlHelper;

  constructor(
    private reporteService: ReporteService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.reporteService.getAllFull().subscribe({
      next: (res) => {
        this.reportes = res.data;
        this.reportesFiltrados = res.data;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  buscar(termino: string): void {
    this.busqueda = termino.toLowerCase();
    this.reportesFiltrados = this.reportes.filter(r =>
      r.mascota.nombre.toLowerCase().includes(this.busqueda) ||
      r.mascota.especie.toLowerCase().includes(this.busqueda) ||
      (r.mascota.raza && r.mascota.raza.toLowerCase().includes(this.busqueda)) ||
      r.direccion.toLowerCase().includes(this.busqueda) ||
      r.estado.toLowerCase().includes(this.busqueda)
    );
  }

  goToDetail(id: number): void {
    this.router.navigate(['/dashboard/mis-reportes', id]);
  }

  crearNuevoReporte(): void {
    this.router.navigate(['/dashboard/crear-reporte']);
  }

  // Estados corregidos a mayúsculas (coinciden con el backend)
  getEstadoClass(estado: string): string {
    const map: Record<string, string> = {
      PERDIDA: 'badge--perdido',
      ENCONTRADA: 'badge--encontrado',
      CERRADA: 'badge--finalizado'
    };
    return map[estado] ?? '';
  }

  getEstadoLabel(estado: string): string {
    const map: Record<string, string> = {
      PERDIDA: '🔴 Perdido',
      ENCONTRADA: '🟢 Encontrado',
      CERRADA: '⚫ Finalizado'
    };
    return map[estado] ?? estado;
  }
}