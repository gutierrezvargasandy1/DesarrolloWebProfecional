// mis-reportes-detalle.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReporteService } from '../../core/services/reportes/reporte.service';
import { ReporteFull } from '../../core/services/reportes/model/reporte';
import { UrlHelper } from '../../core/utils/url.helper';

@Component({
  selector: 'app-mis-reportes-detalle',
  standalone: false,
  templateUrl: './mis-reportes-detalle.html',
  styleUrls: ['./mis-reportes-detalle.css']
})
export class MisReportesDetalle implements OnInit {
  reporte: ReporteFull | null = null;
  loading = true;
  error = false;

  puntosMapa: { lat: number; lng: number; label?: string }[] = [];

  // Exponer el helper en el template
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

    this.reporteService.getFullById(id).subscribe({
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
    if (!this.reporte) return;

    // Coordenada del reporte principal
    if (this.reporte.latitud && this.reporte.longitud) {
      this.puntosMapa.push({
        lat: this.reporte.latitud,
        lng: this.reporte.longitud,
        label: `📍 Reporte: ${this.reporte.descripcion?.substring(0, 50)}`
      });
    }

    // Coordenadas de avistamientos
    if (this.reporte.avistamientos && this.reporte.avistamientos.length) {
      this.reporte.avistamientos.forEach(av => {
        if (av.latitud && av.longitud) {
          this.puntosMapa.push({
            lat: av.latitud,
            lng: av.longitud,
            label: `👁️ Avistamiento: ${av.descripcion?.substring(0, 50)}`
          });
        }
      });
    }
  }

  volver(): void {
    this.router.navigate(['/dashboard/mis-reportes']);
  }

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