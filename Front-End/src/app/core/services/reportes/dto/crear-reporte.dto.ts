export interface CrearReporteDTO {
  id_mascota: number;
  descripcion: string;
  latitud: number;
  longitud: number;
  direccion: string;
  estado?: 'PERDIDA' | 'ENCONTRADA';   // ✅ cambiado
}