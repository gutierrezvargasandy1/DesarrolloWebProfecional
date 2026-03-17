class CrearReporteDTO:
    def __init__(
        self,
        id_mascota: int,
        id_usuario: int,
        descripcion: str,
        latitud: float,
        longitud: float,
        direccion: str = None,
        estado: str = "PERDIDA"
    ):
        self.id_mascota = id_mascota
        self.id_usuario = id_usuario
        self.descripcion = descripcion
        self.latitud = latitud
        self.longitud = longitud
        self.direccion = direccion
        self.estado = estado