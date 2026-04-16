export interface ActualizarMascotaDTO {
  nombre?: string;
  especie?: string;
  raza?: string;
  color?: string;
  sexo?: string;
  edad?: number;
  descripcion?: string;
  estado?: 'PERDIDA' | 'ENCONTRADA' | 'NORMAL' | 'CELO' | 'ADOPCION';
  direccion_hogar?: string;
  latitud_hogar?: number;
  longitud_hogar?: number;
}