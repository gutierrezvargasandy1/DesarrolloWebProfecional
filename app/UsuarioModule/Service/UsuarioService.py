from app import db
from app.UsuarioModule.Model.Usuario import Usuario
from flask_bcrypt import Bcrypt

bcrypt = Bcrypt()

class UsuarioService:

    def get_all(self):
        return Usuario.query.filter_by(activo=True).all()

    def get_by_id(self, id):
        return db.session.get(Usuario, id)

    def get_by_correo(self, correo):
        return Usuario.query.filter_by(correo=correo).first()

    def crear(self, data):
        if self.get_by_correo(data["correo"]):
            raise ValueError("El correo ya está registrado.")

        usuario = Usuario(
            nombre        = data["nombre"],
            correo        = data["correo"],
            password_hash = bcrypt.generate_password_hash(data["password"]).decode("utf-8"),
            telefono      = data.get("telefono")
        )
        db.session.add(usuario)
        db.session.commit()
        return usuario

    def actualizar(self, id, data):
        usuario = self.get_by_id(id)
        if not usuario:
            raise ValueError("Usuario no encontrado.")

        if "nombre"   in data: usuario.nombre   = data["nombre"]
        if "telefono" in data: usuario.telefono = data["telefono"]
        if "password" in data: usuario.password_hash = bcrypt.generate_password_hash(data["password"]).decode("utf-8")

        db.session.commit()
        return usuario

    def eliminar(self, id):
        usuario = self.get_by_id(id)
        if not usuario:
            raise ValueError("Usuario no encontrado.")
        usuario.activo = False  # Soft delete
        db.session.commit()
