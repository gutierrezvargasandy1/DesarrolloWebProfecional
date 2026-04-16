import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReporteService } from '../../core/services/reportes/reporte.service';
import { ReporteConMascota } from '../../core/services/reportes/model/reporte';
import { UrlHelper } from '../../core/utils/url.helper';

@Component({
  selector: 'app-reporte-detalle-publico',
  standalone: false,
  templateUrl: './reporte-detalle-publico.html',
  styleUrls: ['./reporte-detalle-publico.css']
})
export class ReporteDetallePublico implements OnInit {
  reporte: ReporteConMascota | null = null;
  loading = true;
  error = false;

  // Para el mapa (si lo usas)
  puntosMapa: { lat: number; lng: number; label?: string }[] = [];

  // Exponemos el helper
  urlHelper = UrlHelper;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reporteService: ReporteService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (isNaN(id)) {
      this.error = true;
      this.loading = false;
      return;
    }

    this.reporteService.getPublicById(id).subscribe({
      next: (res) => {
        this.reporte = res.data;
        this.cargarCoordenadas();
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  private cargarCoordenadas(): void {
    if (this.reporte && this.reporte.latitud && this.reporte.longitud) {
      this.puntosMapa = [{
        lat: this.reporte.latitud,
        lng: this.reporte.longitud,
        label: `📍 ${this.reporte.direccion}`
      }];
    }
  }

  volver(): void {
    this.router.navigate(['/']); // o a la lista pública
  }

  // Estados corregidos (mayúsculas)
  getBadgeClass(estado: string): string {
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