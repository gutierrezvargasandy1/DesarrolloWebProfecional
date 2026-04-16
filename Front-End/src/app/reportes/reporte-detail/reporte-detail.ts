import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReporteConMascota } from '../../core/services/reportes/model/reporte';
import { ReporteService } from '../../core/services/reportes/reporte.service';
import { LoadingService } from '../../core/services/api/interceptors/loading.interceptor';

@Component({
  selector: 'app-reporte-detail',
  standalone: false,
  templateUrl: './reporte-detail.html',
  styleUrls: ['./reporte-detail.css']
})
export class ReporteDetail implements OnInit {
  reporte: ReporteConMascota | null = null;
  errorMessage: string = '';
  isLoading: boolean = false;
  idReporte: number = 0;

  // Propiedad para pasar al mapa
  coordenadasMapa: { lat: number, lng: number, label?: string }[] = [];

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

    this.reporteService.getPublicById(this.idReporte).subscribe({
      next: (response) => {
        this.reporte = response.data;
        this.isLoading = false;
        this.loadingService.hide();

        // Si el reporte tiene coordenadas, las agregamos al array para el mapa
        if (this.reporte && this.reporte.latitud && this.reporte.longitud) {
          this.coordenadasMapa = [{
            lat: this.reporte.latitud,
            lng: this.reporte.longitud,
            label: `📍 ${this.reporte.mascota?.nombre || 'Mascota'} - ${this.reporte.estado}`
          }];
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.loadingService.hide();
        this.errorMessage = err?.message || 'Error al cargar el reporte. Intenta de nuevo.';
        console.error('Error loading report detail:', err);
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
    return new Date(fecha).toLocaleDateString('es-ES', {
      day: '2-digit', month: 'long', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  volverALista(): void {
    this.router.navigate(['/lista']);
  }

  // Genera URL de Google Maps con las coordenadas
  getMapUrl(): string {
    if (this.reporte?.latitud && this.reporte?.longitud) {
      return `https://www.google.com/maps?q=${this.reporte.latitud},${this.reporte.longitud}&z=15`;
    }
    return '';
  }

  // reporte-detail.ts (fragmento agregado al final)
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