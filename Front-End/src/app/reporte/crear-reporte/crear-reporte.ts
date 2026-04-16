// crear-reporte.component.ts
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, Subscription, EMPTY } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import { ReporteService } from '../../core/services/reportes/reporte.service';
import { MascotaService } from '../../core/services/mascotas/mascota.service';
import { GeocodingService } from '../../core/services/geocoding/geocoding.service';

type EstadoGeocoding = 'idle' | 'buscando' | 'encontrado' | 'no-encontrado' | 'error';

@Component({
  selector: 'app-crear-reporte',
  standalone: false,
  templateUrl: './crear-reporte.html',
  styleUrls: ['./crear-reporte.css']
})
export class CrearReporte implements OnInit, OnDestroy {
  reporteForm: FormGroup;
  mascotas: any[] = [];
  loading = false;

  // Lista completa de estados disponibles para el reporte (coincide con el enum)
  estados = [
    { value: 'PERDIDA', label: '🔴 Perdida' },
    { value: 'ENCONTRADA', label: '🟢 Encontrada' },
    { value: 'NORMAL', label: '⚪ Normal' },
    { value: 'CELO', label: '💕 Celo' },
    { value: 'ADOPCION', label: '🏠 Adopción' }
  ];

  estadoGeocoding: EstadoGeocoding = 'idle';
  direccionEncontrada?: string;

  private direccion$ = new Subject<string>();
  private sub = new Subscription();

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private reporteService: ReporteService,
    private mascotaService: MascotaService,
    private geocoding: GeocodingService
  ) {
    this.reporteForm = this.fb.group({
      id_mascota: ['', Validators.required],
      estado: ['PERDIDA', Validators.required],
      descripcion: ['', Validators.required],
      direccion: ['', Validators.required],
      latitud: [null, Validators.required],
      longitud: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarMascotas();

    this.sub.add(
      this.direccion$
        .pipe(
          debounceTime(600),
          distinctUntilChanged(),
          switchMap(dir => {
            if (!dir || dir.trim().length < 3) {
              this.estadoGeocoding = 'idle';
              this.reporteForm.patchValue({ latitud: null, longitud: null });
              this.direccionEncontrada = undefined;
              return EMPTY;
            }
            this.estadoGeocoding = 'buscando';
            return this.geocoding.buscarCoordenadas(dir);
          })
        )
        .subscribe(coords => {
          if (!coords) {
            this.estadoGeocoding = 'no-encontrado';
            this.reporteForm.patchValue({ latitud: null, longitud: null });
            this.direccionEncontrada = undefined;
            return;
          }
          this.reporteForm.patchValue({
            latitud: coords.lat,
            longitud: coords.lon
          });
          this.direccionEncontrada = coords.displayName;
          this.estadoGeocoding = 'encontrado';
        })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  onDireccionChange(valor: string): void {
    this.direccion$.next(valor);
  }

  cargarMascotas(): void {
    this.mascotaService.getMisMascotas().subscribe({
      next: (res) => (this.mascotas = res.data || []),
      error: (err) => console.error('Error cargando mascotas:', err)
    });
  }

  onSubmit(): void {
    if (this.reporteForm.invalid) return;
    this.loading = true;
    this.reporteService.create(this.reporteForm.value).subscribe({
      next: (res) => {
        this.loading = false;
        this.router.navigate(['/dashboard/mis-reportes', res.data.id_reporte]);
      },
      error: (err) => {
        this.loading = false;
        console.error(err);
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/dashboard/mis-reportes']);
  }
}