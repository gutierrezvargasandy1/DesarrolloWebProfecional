import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../api/endpoints';
import { ApiResponse } from '../api/model/api.responce';
import { Mascota } from './model/mascota';
import { CrearMascotaDTO } from './dto/CrearMascotaDTO';
import { ActualizarMascotaDTO } from './dto/ActualizarMascotaDTO';
import { ApiService } from '../api/api.service';

@Injectable({ providedIn: 'root' })
export class MascotaService {

  constructor(private api: ApiService) {}

  /**
   * Obtener todas las mascotas del usuario autenticado.
   */
  getMisMascotas(): Observable<ApiResponse<Mascota[]>> {
    return this.api.get(API_ENDPOINTS.MASCOTAS.GET_ALL);
  }

  /**
   * Obtener una mascota específica por ID.
   */
  getMascotaById(id: number): Observable<ApiResponse<Mascota>> {
    return this.api.get(API_ENDPOINTS.MASCOTAS.GET_BY_ID(id));
  }

  /**
   * Crear una nueva mascota con foto opcional.
   */
  crearMascota(
    dto: CrearMascotaDTO,
    foto?: File
  ): Observable<ApiResponse<{ id_mascota: number }>> {

    const formData = new FormData();

    Object.entries(dto).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        formData.append(key, value.toString());
      }
    });

    if (foto) {
      formData.append('foto', foto);
    }

    return this.api.post(
      API_ENDPOINTS.MASCOTAS.CREATE,
      formData
    );
  }

  /**
   * Actualizar una mascota existente con foto opcional.
   */
  actualizarMascota(
    id: number,
    dto: ActualizarMascotaDTO,
    foto?: File
  ): Observable<ApiResponse<{ id_mascota: number }>> {

    const formData = new FormData();

    Object.entries(dto).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        formData.append(key, value.toString());
      }
    });

    if (foto) {
      formData.append('foto', foto);
    }

    return this.api.put(
      API_ENDPOINTS.MASCOTAS.UPDATE(id),
      formData
    );
  }

  /**
   * Eliminar una mascota.
   */
  eliminarMascota(id: number): Observable<ApiResponse<boolean>> {
    return this.api.delete(API_ENDPOINTS.MASCOTAS.DELETE(id));
  }
}