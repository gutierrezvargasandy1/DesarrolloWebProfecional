import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MascotaService } from '../../core/services/mascotas/mascota.service';
import { Mascota } from '../../core/services/mascotas/model/mascota';
import { UrlHelper } from '../../core/utils/url.helper';

@Component({
  selector: 'app-mis-mascotas',
  standalone: false,
  templateUrl: './mis-mascotas.html',
  styleUrl: './mis-mascotas.css',
})
export class MisMascotas implements OnInit {
  mascotas: Mascota[] = [];
  mascotasFiltradas: Mascota[] = [];
  loading = true;
  error = false;

  // 👇 ESTO ES LO IMPORTANTE
  UrlHelper = UrlHelper;

  constructor(
    private mascotaService: MascotaService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.mascotaService.getMisMascotas().subscribe({
      next: (res) => {
        this.mascotas = res.data;
        this.mascotasFiltradas = res.data;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  buscar(termino: string): void {
    const t = termino.toLowerCase();
    this.mascotasFiltradas = this.mascotas.filter(m =>
      m.nombre.toLowerCase().includes(t) ||
      m.especie.toLowerCase().includes(t) ||
      (m.raza ?? '').toLowerCase().includes(t) ||
      (m.color ?? '').toLowerCase().includes(t) ||
      m.estado.toLowerCase().includes(t)
    );
  }

  goToDetail(id: number): void {
    this.router.navigate(['/dashboard/mis-mascotas', id]);
  }

  goToCrear(): void {
    this.router.navigate(['/dashboard/mis-mascotas/nueva']);
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
}