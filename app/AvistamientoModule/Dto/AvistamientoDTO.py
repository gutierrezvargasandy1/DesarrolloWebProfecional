class AvistamientoDTO:
    def __init__(self, avistamiento, usuario=None, reporte=None):
        self.id_avistamiento   = avistamiento.id_avistamiento
        self.id_reporte        = avistamiento.id_reporte
        self.id_usuario        = avistamiento.id_usuario
        self.descripcion       = avistamiento.descripcion
        self.latitud           = float(avistamiento.latitud)
        self.longitud          = float(avistamiento.longitud)
        self.foto_url          = avistamiento.foto_url
        self.fecha_avistamiento = avistamiento.fecha_avistamiento

        # Info del usuario que reportó el avistamiento (opcional)
        self.usuario = None
        if usuario:
            self.usuario = {
                "id_usuario": usuario.id_usuario,
                "nombre":     usuario.nombre,
                "correo":     usuario.correo,
                "telefono":   usuario.telefono
            }

        # Info básica del reporte relacionado (opcional)
        self.reporte = None
        if reporte:
            self.reporte = {
                "id_reporte":  reporte.id_reporte,
                "id_mascota":  reporte.id_mascota,
                "descripcion": reporte.descripcion,
                "estado":      reporte.estado
            }

    def to_dict(self):
        return {
            "id_avistamiento":    self.id_avistamiento,
            "id_reporte":         self.id_reporte,
            "id_usuario":         self.id_usuario,
            "descripcion":        self.descripcion,
            "latitud":            self.latitud,
            "longitud":           self.longitud,
            "foto_url":           self.foto_url,
            "fecha_avistamiento": self.fecha_avistamiento.isoformat() if self.fecha_avistamiento else None,
            "usuario":            self.usuario,
            "reporte":            self.reporte
        }