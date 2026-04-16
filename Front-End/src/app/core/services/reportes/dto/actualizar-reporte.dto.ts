export interface ActualizarReporteDTO {
  descripcion?: string;
  latitud?: number;
  longitud?: number;
  direccion?: string;
  estado?: 'perdido' | 'encontrado' | 'finalizado';
}