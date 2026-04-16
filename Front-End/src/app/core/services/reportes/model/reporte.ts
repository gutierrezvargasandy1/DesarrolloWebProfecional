export interface Reporte {
  id_reporte: number;
  id_mascota: number;
  descripcion: string;
  latitud: number;
  longitud: number;
  direccion: string;
  estado: 'PERDIDA' | 'ENCONTRADA' | 'CERRADA';   // ✅ cambiado
  fecha_reporte: string;   
  usuario_id: number;
}

export interface ReporteConMascota extends Reporte {
  mascota: {
    id_mascota: number;
    nombre: string;
    especie: string;
    raza: string;
    foto_url?: string;
  };
}

export interface ReporteFull extends ReporteConMascota {
  avistamientos?: Avistamiento[];
}

export interface Avistamiento {
  id_avistamiento: number;
  reporte_id: number;
  usuario_id: number;
  latitud: number;
  longitud: number;
  direccion: string;
  descripcion: string;
  fecha_avistamiento: string;
}