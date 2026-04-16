import { Component, OnInit } from '@angular/core';
import { ReporteConMascota } from '../../core/services/reportes/model/reporte';
import { ReporteService } from '../../core/services/reportes/reporte.service';
import { LoadingService } from '../../core/services/api/interceptors/loading.interceptor';

@Component({
  selector: 'app-lista',
  standalone: false,
  templateUrl: './lista.html',
  styleUrls: ['./lista.css']
})
export class Lista implements OnInit {
  reportes: ReporteConMascota[] = [];
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private reporteService: ReporteService,
    private loadingService: LoadingService
  ) {}

  ngOnInit(): void {
    this.cargarReportes();
  }

  cargarReportes(): void {
    this.isLoading = true;
    this.loadingService.show();
    this.errorMessage = '';

    this.reporteService.getAllPublic().subscribe({
      next: (response) => {
        this.reportes = response.data || [];
        this.isLoading = false;
        this.loadingService.hide();
      },
      error: (err) => {
        this.isLoading = false;
        this.loadingService.hide();
        this.errorMessage = err?.message || 'Error al cargar los reportes. Intenta de nuevo.';
        console.error('Error loading reports:', err);
      }
    });
  }

  // Helper para obtener la URL de la foto o un placeholder
  getFotoUrl(mascota: any): string {
    return mascota?.foto_url || 'assets/default-pet.png';
  }

  // Helper para obtener el color según estado
  getEstadoColor(estado: string): string {
    switch (estado) {
      case 'perdido': return '#e74c3c';
      case 'encontrado': return '#2ecc71';
      default: return '#f39c12';
    }
  }

  // Formatear fecha (opcional)
  formatearFecha(fecha: string): string {
    if (!fecha) return '';
    return new Date(fecha).toLocaleDateString('es-ES', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }
}