import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReporteService } from '../../core/services/reportes/reporte.service';
import { LoadingService } from '../../core/services/api/interceptors/loading.interceptor';
import { ReporteConMascota } from '../../core/services/reportes/model/reporte';

@Component({
  selector: 'app-lista',
  standalone: false,
  templateUrl: './lista.html',
  styleUrls: ['./lista.css']
})
export class Lista implements OnInit {
  reportes: ReporteConMascota[] = [];
  errorMessage = '';
  isLoading = false;

  constructor(
    private reporteService: ReporteService,
    private loadingService: LoadingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarMisReportes();
  }

  cargarMisReportes(): void {
    this.isLoading = true;
    this.loadingService.show();
    this.errorMessage = '';

    this.reporteService.getAllFull().subscribe({
      next: (response) => {
        this.reportes = response.data || [];
        this.isLoading = false;
        this.loadingService.hide();
      },
      error: (err) => {
        this.isLoading = false;
        this.loadingService.hide();
        this.errorMessage = err?.message || 'Error al cargar tus reportes.';
        console.error(err);
      }
    });
  }

  getEstadoColor(estado: string): string {
    switch (estado) {
      case 'perdido': return '#e74c3c';
      case 'encontrado': return '#2ecc71';
      default: return '#f39c12';
    }
  }

  getFotoUrl(mascota: any): string {
    return mascota?.foto_url || 'assets/default-pet.png';
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '';
    return new Date(fecha).toLocaleDateString('es-ES');
  }

  verDetalle(id: number): void {
    this.router.navigate(['/reporte', id]);  // Ruta que usará el componente ReporteDetail
  }
}