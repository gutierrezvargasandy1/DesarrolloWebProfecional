import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MascotaService } from '../../core/services/mascotas/mascota.service';
import { ReporteService } from '../../core/services/reportes/reporte.service';
import { Mascota } from '../../core/services/mascotas/model/mascota';
import { ReporteConMascota } from '../../core/services/reportes/model/reporte';
import { UrlHelper } from '../../core/utils/url.helper';

@Component({
  selector: 'app-mascota-detalle',
  standalone: false,
  templateUrl: './detalle-mascotas.html',
  styleUrls: ['./detalle-mascotas.css']
})
export class DetalleMascotas implements OnInit {
  UrlHelper = UrlHelper;

  mascota: Mascota | null = null;
  reportes: ReporteConMascota[] = [];
  loading = true;
  error = false;

  puntosMapa: { lat: number; lng: number; label?: string }[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private mascotaService: MascotaService,
    private reporteService: ReporteService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.mascotaService.getMascotaById(id).subscribe({
      next: (res) => {
        this.mascota = res.data;
        this.actualizarPuntosMapa();
        this.cargarReportes();
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  cargarReportes(): void {
    if (!this.mascota) return;

    this.reporteService.getAllFull().subscribe({
      next: (res) => {
        if (res.data && Array.isArray(res.data)) {
          // 🔥 Filtro corregido: usa mascota.id_mascota
          this.reportes = res.data.filter(reporte => {
            const idMascotaReporte = Number(reporte.mascota?.id_mascota);
            const idMascotaActual = Number(this.mascota?.id_mascota);
            return idMascotaReporte === idMascotaActual;
          });
        } else {
          this.reportes = [];
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar reportes:', err);
        this.reportes = [];
        this.loading = false;
      }
    });
  }

  private actualizarPuntosMapa(): void {
    if (this.mascota?.latitud_hogar && this.mascota?.longitud_hogar) {
      this.puntosMapa = [{
        lat: this.mascota.latitud_hogar,
        lng: this.mascota.longitud_hogar,
        label: this.mascota.nombre
      }];
    } else {
      this.puntosMapa = [];
    }
  }

  volver(): void {
    this.router.navigate(['/dashboard/mis-mascotas']);
  }

  goToReporte(id: number): void {
    this.router.navigate(['/dashboard/mis-reportes', id]);
  }

  getEstadoClass(estado: string): string {
    const map: Record<string, string> = {
      PERDIDA: 'badge--perdida',
      ENCONTRADA: 'badge--encontrada',
      NORMAL: 'badge--normal',
      CELO: 'badge--celo',
      ADOPCION: 'badge--adopcion',
    };
    return map[estado] ?? '';
  }

  getEstadoLabel(estado: string): string {
    const map: Record<string, string> = {
      PERDIDA: '🔴 Perdida',
      ENCONTRADA: '🟢 Encontrada',
      NORMAL: '🔵 Normal',
      CELO: '🟡 En celo',
      ADOPCION: '🟣 En adopción',
    };
    return map[estado] ?? estado;
  }

  getReporteEstadoClass(estado: string): string {
    const map: Record<string, string> = {
      PERDIDA: 'badge--perdido',
      ENCONTRADA: 'badge--encontrado',
      CERRADA: 'badge--finalizado',
    };
    return map[estado] ?? '';
  }

  getReporteEstadoLabel(estado: string): string {
    const map: Record<string, string> = {
      PERDIDA: '🔴 Perdido',
      ENCONTRADA: '🟢 Encontrado',
      CERRADA: '⚫ Finalizado',
    };
    return map[estado] ?? estado;
  }
}