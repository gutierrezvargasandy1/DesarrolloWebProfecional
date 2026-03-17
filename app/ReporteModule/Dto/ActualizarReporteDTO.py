class ActualizarReporteDTO:
    def __init__(
        self,
        descripcion: str = None,
        latitud: float = None,
        longitud: float = None,
        direccion: str = None,
        estado: str = None
    ):
        self.descripcion = descripcion
        self.latitud = latitud
        self.longitud = longitud
        self.direccion = direccion
        self.estado = estado