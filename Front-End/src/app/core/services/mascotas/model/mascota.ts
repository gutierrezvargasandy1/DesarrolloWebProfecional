// src/app/core/services/mascotas/model/mascota.ts

export interface Mascota {
  id_mascota: number;
  id_usuario?: number;
  nombre: string;
  especie: string;
  raza?: string;
  color?: string;
  sexo?: string;
  edad?: number;
  descripcion?: string;
  foto_url?: string;
  estado: 'PERDIDA' | 'ENCONTRADA' | 'NORMAL' | 'CELO' | 'ADOPCION';
  direccion_hogar?: string;
  latitud_hogar?: number | null;
  longitud_hogar?: number | null;
  fecha_registro?: string;
}


