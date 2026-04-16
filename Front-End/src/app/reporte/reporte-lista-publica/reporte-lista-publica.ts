import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { ReporteService } from '../../core/services/reportes/reporte.service';
import { ReporteConMascota } from '../../core/services/reportes/model/reporte';
import { UrlHelper } from '../../core/utils/url.helper';

@Component({
  selector: 'app-reporte-lista-publica',
  standalone: false,
  templateUrl: './reporte-lista-publica.html',
  styleUrls: ['./reporte-lista-publica.css']
})
export class ReporteListaPublica implements OnInit {
  reportes: ReporteConMascota[] = [];
  loading = true;   // inicializamos en true
  error = false;

  urlHelper = UrlHelper;

  constructor(
    private reporteService: ReporteService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarReportes();
  }

  cargarReportes(): void {
    this.loading = true;
    this.error = false;
    this.reporteService.getAllPublic().subscribe({
      next: (res) => {
        this.reportes = res.data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  goToDetail(id: number): void {
    this.router.navigate(['/dashboard/reporte', id]);
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