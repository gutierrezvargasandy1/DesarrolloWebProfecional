class MascotaReporteDTO:
    def __init__(self, mascota, reporte):
        self.id_reporte = reporte.id_reporte
        self.descripcion_reporte = reporte.descripcion
        self.latitud = float(reporte.latitud)
        self.longitud = float(reporte.longitud)
        self.direccion = reporte.direccion
        self.estado_reporte = reporte.estado
        self.fecha_reporte = reporte.fecha_reporte

        self.mascota = {
            "id_mascota": mascota.id_mascota,
            "nombre": mascota.nombre,
            "especie": mascota.especie,
            "raza": mascota.raza,
            "color": mascota.color,
            "sexo": mascota.sexo,
            "edad": mascota.edad,
            "descripcion": mascota.descripcion,
            "foto_url": mascota.foto_url,
            "estado": mascota.estado,
            "direccion_hogar": mascota.direccion_hogar,
            "latitud_hogar": float(mascota.latitud_hogar) if mascota.latitud_hogar else None,
            "longitud_hogar": float(mascota.longitud_hogar) if mascota.longitud_hogar else None
        }

    def to_dict(self):
        return {
            "id_reporte": self.id_reporte,
            "descripcion": self.descripcion_reporte,
            "latitud": self.latitud,
            "longitud": self.longitud,
            "direccion": self.direccion,
            "estado": self.estado_reporte,
            "fecha_reporte": self.fecha_reporte.isoformat() if self.fecha_reporte else None,
            "mascota": self.mascota
        }