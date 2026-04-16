// services/reporte/reporte.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../api/api.service';
import { API_ENDPOINTS } from '../api/endpoints';
import { ApiResponse } from '../api/model/api.responce';

import {
  Avistamiento,
  ReporteConMascota,
  ReporteFull
} from './model/reporte';

import { CrearReporteDTO } from './dto/crear-reporte.dto';
import { ActualizarReporteDTO } from './dto/actualizar-reporte.dto';
import {
  ActualizarAvistamientoDTO,
  CrearAvistamientoDTO
} from './dto/avistamiento.dto';

@Injectable({ providedIn: 'root' })
export class ReporteService {

  constructor(private api: ApiService) {}

  // =========================
  // REPORTES PROPIOS
  // =========================

  getAll(): Observable<ApiResponse<number[]>> {
    return this.api.get(API_ENDPOINTS.REPORTES.GET_ALL);
  }

  getById(id: number): Observable<ApiResponse<{ id_reporte: number }>> {
    return this.api.get(API_ENDPOINTS.REPORTES.GET_BY_ID(id));
  }

  getAllFull(): Observable<ApiResponse<ReporteConMascota[]>> {
    return this.api.get(API_ENDPOINTS.REPORTES.GET_ALL_FULL);
  }

  getFullById(id: number): Observable<ApiResponse<ReporteFull>> {
    return this.api.get(API_ENDPOINTS.REPORTES.GET_FULL_BY_ID(id));
  }

  create(dto: CrearReporteDTO): Observable<ApiResponse<{ id_reporte: number }>> {
    return this.api.post(API_ENDPOINTS.REPORTES.CREATE, dto);
  }

  update(
    id: number,
    dto: ActualizarReporteDTO
  ): Observable<ApiResponse<{ id_reporte: number }>> {
    return this.api.put(API_ENDPOINTS.REPORTES.UPDATE(id), dto);
  }

  delete(id: number): Observable<ApiResponse<null>> {
    return this.api.delete(API_ENDPOINTS.REPORTES.DELETE(id));
  }

  // =========================
  // PUBLICOS
  // =========================

  getAllPublic(): Observable<ApiResponse<ReporteConMascota[]>> {
    return this.api.get(API_ENDPOINTS.REPORTES.GET_ALL_PUBLIC);
  }

  getPublicById(id: number): Observable<ApiResponse<ReporteConMascota>> {
    return this.api.get(API_ENDPOINTS.REPORTES.GET_PUBLIC_BY_ID(id));
  }

  // =========================
  // AVISTAMIENTOS
  // =========================

  createAvistamiento(
    idReporte: number,
    dto: CrearAvistamientoDTO
  ): Observable<ApiResponse<Avistamiento>> {
    return this.api.post(
      API_ENDPOINTS.REPORTES.CREATE_AVISTAMIENTO(idReporte),
      dto
    );
  }

  updateAvistamiento(
    idAvistamiento: number,
    dto: ActualizarAvistamientoDTO
  ): Observable<ApiResponse<Avistamiento>> {
    return this.api.put(
      API_ENDPOINTS.REPORTES.UPDATE_AVISTAMIENTO(idAvistamiento),
      dto
    );
  }
}