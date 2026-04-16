// services/reporte/reporte.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../api/api.service';
import { API_ENDPOINTS } from '../api/endpoints';
import { ApiResponse } from '../api/model/api.responce';
import { Avistamiento, ReporteConMascota, ReporteFull } from './model/reporte';
import { CrearReporteDTO } from './dto/crear-reporte.dto';
import { ActualizarReporteDTO } from './dto/actualizar-reporte.dto';
import { ActualizarAvistamientoDTO, CrearAvistamientoDTO } from './dto/avistamiento.dto';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  constructor(private api: ApiService) {}

  // ========== Reportes propios (requieren autenticación) ==========

  /** Obtener todos los reportes del usuario autenticado (solo IDs) */
  getAll(): Observable<ApiResponse<number[]>> {
    return this.api.get<ApiResponse<number[]>>(API_ENDPOINTS.REPORTES.GET_ALL);
  }

  /** Obtener un reporte por ID (propio) */
  getById(id: number): Observable<ApiResponse<{ id_reporte: number }>> {
    return this.api.get<ApiResponse<{ id_reporte: number }>>(
      API_ENDPOINTS.REPORTES.GET_BY_ID(id)
    );
  }

  /** Obtener todos los reportes del usuario con datos de mascota (full) */
  getAllFull(): Observable<ApiResponse<ReporteConMascota[]>> {
    return this.api.get<ApiResponse<ReporteConMascota[]>>(
      API_ENDPOINTS.REPORTES.GET_ALL_FULL
    );
  }

  /** Obtener un reporte completo (con mascota y avistamientos) por ID y usuario */
  getFullById(id: number): Observable<ApiResponse<ReporteFull>> {
    return this.api.get<ApiResponse<ReporteFull>>(
      API_ENDPOINTS.REPORTES.GET_FULL_BY_ID(id)
    );
  }

  /** Crear un nuevo reporte */
  create(dto: CrearReporteDTO): Observable<ApiResponse<{ id_reporte: number }>> {
    return this.api.post<ApiResponse<{ id_reporte: number }>>(
      API_ENDPOINTS.REPORTES.CREATE,
      dto
    );
  }

  /** Actualizar un reporte existente */
  update(id: number, dto: ActualizarReporteDTO): Observable<ApiResponse<{ id_reporte: number }>> {
    return this.api.put<ApiResponse<{ id_reporte: number }>>(
      API_ENDPOINTS.REPORTES.UPDATE(id),
      dto
    );
  }

  /** Eliminar un reporte */
  delete(id: number): Observable<ApiResponse<null>> {
    return this.api.delete<ApiResponse<null>>(API_ENDPOINTS.REPORTES.DELETE(id));
  }

  // ========== Reportes públicos (no requieren autenticación) ==========

  /** Obtener todos los reportes públicos con datos de mascota */
  getAllPublic(): Observable<ApiResponse<ReporteConMascota[]>> {
    return this.api.get<ApiResponse<ReporteConMascota[]>>(
      API_ENDPOINTS.REPORTES.GET_ALL_PUBLIC
    );
  }

  /** Obtener un reporte público por ID con datos de mascota */
  getPublicById(id: number): Observable<ApiResponse<ReporteConMascota>> {
    return this.api.get<ApiResponse<ReporteConMascota>>(
      API_ENDPOINTS.REPORTES.GET_PUBLIC_BY_ID(id)
    );
  }

  // ========== Avistamientos ==========

  /** Crear un avistamiento asociado a un reporte */
  createAvistamiento(
    idReporte: number,
    dto: CrearAvistamientoDTO
  ): Observable<ApiResponse<Avistamiento>> {
    return this.api.post<ApiResponse<Avistamiento>>(
      API_ENDPOINTS.REPORTES.CREATE_AVISTAMIENTO(idReporte),
      dto
    );
  }

  /** Actualizar un avistamiento existente */
  updateAvistamiento(
    idAvistamiento: number,
    dto: ActualizarAvistamientoDTO
  ): Observable<ApiResponse<Avistamiento>> {
    return this.api.put<ApiResponse<Avistamiento>>(
      API_ENDPOINTS.REPORTES.UPDATE_AVISTAMIENTO(idAvistamiento),
      dto
    );
  }
}