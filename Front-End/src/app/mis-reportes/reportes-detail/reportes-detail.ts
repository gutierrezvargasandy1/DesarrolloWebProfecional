import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReporteService } from '../../core/services/reportes/reporte.service';
import { LoadingService } from '../../core/services/api/interceptors/loading.interceptor';
import { ReporteFull } from '../../core/services/reportes/model/reporte';

@Component({
  selector: 'app-reportes-detail',
  standalone: false,
  templateUrl: './reportes-detail.html',
  styleUrls: ['./reportes-detail.css']
})
export class ReporteDetail implements OnInit {

  reporte: ReporteFull | null = null;
  errorMessage = '';
  isLoading = false;
  idReporte = 0;
  coordenadasMapa: { lat: number; lng: number; label?: string }[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reporteService: ReporteService,
    private loadingService: LoadingService
  ) {}

  ngOnInit(): void {
    this.idReporte = Number(this.route.snapshot.paramMap.get('id'));
    if (isNaN(this.idReporte) || this.idReporte <= 0) {
      this.errorMessage = 'ID de reporte inválido';
      return;
    }
    this.cargarReporte();
  }

  cargarReporte(): void {
    this.isLoading = true;
    this.loadingService.show();
    this.errorMessage = '';

    this.reporteService.getFullById(this.idReporte).subscribe({
      next: (response) => {
        this.reporte = response.data;
        this.isLoading = false;
        this.loadingService.hide();

        if (this.reporte?.latitud && this.reporte?.longitud) {
          this.coordenadasMapa = [{
            lat: this.reporte.latitud,
            lng: this.reporte.longitud,
            label: `📍 ${this.reporte.mascota?.nombre} - ${this.reporte.estado}`
          }];
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.loadingService.hide();
        this.errorMessage = err?.message || 'Error al cargar el reporte.';
        console.error(err);
      }
    });
  }

  getFotoUrl(): string {
    return this.reporte?.mascota?.foto_url || 'assets/default-pet.png';
  }

  getEstadoColor(estado: string): string {
    switch (estado) {
      case 'perdido': return '#e74c3c';
      case 'encontrado': return '#2ecc71';
      default: return '#f39c12';
    }
  }

  getEstadoIcono(estado: string): string {
    switch (estado) {
      case 'perdido': return '🐾';
      case 'encontrado': return '🏠';
      default: return '❓';
    }
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return 'Fecha no disponible';
    return new Date(fecha).toLocaleString('es-ES');
  }

  volverALista(): void {
    this.router.navigate(['/lista']);  // vuelve a la lista de mis reportes
  }

  getMapUrl(): string {
    if (this.reporte?.latitud && this.reporte?.longitud) {
      return `https://www.google.com/maps?q=${this.reporte.latitud},${this.reporte.longitud}&z=15`;
    }
    return '';
  }

  compartir(): void {
    const url = window.location.href;
    if (navigator.share) {
      navigator.share({
        title: `Mascota ${this.reporte?.mascota?.nombre} - ${this.reporte?.estado}`,
        text: this.reporte?.descripcion,
        url: url
      }).catch(err => console.log('Error sharing', err));
    } else {
      navigator.clipboard.writeText(url);
      alert('Enlace copiado al portapapeles');
    }
  }

  reportarAvistamiento(): void {
    if (this.reporte?.id_reporte) {
      this.router.navigate(['/reportar-avistamiento', this.reporte.id_reporte]);
    }
  }
}