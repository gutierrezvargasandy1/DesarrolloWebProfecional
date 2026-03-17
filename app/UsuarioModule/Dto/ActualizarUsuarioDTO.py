class ActualizarUsuarioDTO:
    def __init__(self, nombre: str = None, correo: str = None, password: str = None, telefono: str = None, activo: bool = None):
        self.nombre = nombre
        self.correo = correo
        self.password = password
        self.telefono = telefono
        self.activo = activo