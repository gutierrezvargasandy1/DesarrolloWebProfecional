import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, Subscription, EMPTY } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import { MascotaService } from '../../core/services/mascotas/mascota.service';
import { CrearMascotaDTO } from '../../core/services/mascotas/dto/CrearMascotaDTO';
import { GeocodingService } from '../../core/services/geocoding/geocoding.service';

type EstadoGeocoding = 'idle' | 'buscando' | 'encontrado' | 'no-encontrado' | 'error';

@Component({
  selector: 'app-crea-mascota',
  standalone: false,
  templateUrl: './crea-mascota.html',
  styleUrl: './crea-mascota.css',
})
export class CreaMascota implements OnInit, OnDestroy {

  dto: CrearMascotaDTO = {
    especie: '',
    estado: 'NORMAL',
  };
  foto?: File;
  fotoPreview?: string;

  estadoGeocoding: EstadoGeocoding = 'idle';
  direccionEncontrada?: string;

  guardando = false;
  errorGuardado?: string;

  especies = ['Perro', 'Gato', 'Ave', 'Conejo', 'Roedor', 'Reptil', 'Pez', 'Otro'];
  sexos = ['MACHO', 'HEMBRA', 'DESCONOCIDO'];
  estados: Array<'PERDIDA' | 'ENCONTRADA' | 'NORMAL' | 'CELO' | 'ADOPCION'> =
    ['NORMAL', 'PERDIDA', 'ENCONTRADA', 'CELO', 'ADOPCION'];

  private direccion$ = new Subject<string>();
  private sub = new Subscription();

  constructor(
    private mascotaService: MascotaService,
    private geocoding: GeocodingService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.sub.add(
      this.direccion$
        .pipe(
          debounceTime(600),
          distinctUntilChanged(),
          switchMap(dir => {
            if (!dir || dir.trim().length < 3) {
              this.estadoGeocoding = 'idle';
              this.dto.latitud_hogar = undefined;
              this.dto.longitud_hogar = undefined;
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
            this.dto.latitud_hogar = undefined;
            this.dto.longitud_hogar = undefined;
            this.direccionEncontrada = undefined;
            return;
          }
          this.dto.latitud_hogar = coords.lat;
          this.dto.longitud_hogar = coords.lon;
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

  seleccionarFoto(e: Event): void {
    const input = e.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    this.foto = input.files[0];

    const reader = new FileReader();
    reader.onload = () => (this.fotoPreview = reader.result as string);
    reader.readAsDataURL(this.foto);
  }

  quitarFoto(): void {
    this.foto = undefined;
    this.fotoPreview = undefined;
  }

  guardar(): void {
    if (!this.dto.especie) {
      this.errorGuardado = 'La especie es obligatoria.';
      return;
    }

    this.guardando = true;
    this.errorGuardado = undefined;

    this.mascotaService.crearMascota(this.dto, this.foto).subscribe({
      next: () => {
        this.guardando = false;
        this.router.navigate(['/dashboard/mis-mascotas']);
      },
      error: (err) => {
        this.guardando = false;
        this.errorGuardado = err?.error?.message || 'No se pudo guardar la mascota.';
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/dashboard/mis-mascotas']);
  }
}