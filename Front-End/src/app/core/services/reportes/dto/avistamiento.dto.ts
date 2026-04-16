export interface CrearAvistamientoDTO {
  latitud: number;
  longitud: number;
  direccion: string;
  descripcion: string;
  fecha_avistamiento?: string; // opcional, si no se envía usa fecha actual
}

export interface ActualizarAvistamientoDTO {
  latitud?: number;
  longitud?: number;
  direccion?: string;
  descripcion?: string;
  fecha_avistamiento?: string;
}