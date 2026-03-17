class CrearUsuarioDTO:
    def __init__(self, nombre: str, correo: str, password: str, telefono: str = None):
        self.nombre = nombre
        self.correo = correo
        self.password = password
        self.telefono = telefono