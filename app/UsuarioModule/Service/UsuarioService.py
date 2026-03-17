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

    # ✅ CREAR CON DTO
    def crear(self, dto):
        if self.get_by_correo(dto.correo):
            raise ValueError("El correo ya está registrado.")

        usuario = Usuario(
            nombre        = dto.nombre,
            correo        = dto.correo,
            password_hash = bcrypt.generate_password_hash(dto.password).decode("utf-8"),
            telefono      = dto.telefono
        )

        db.session.add(usuario)
        db.session.commit()
        return usuario

    # ✅ ACTUALIZAR CON DTO
    def actualizar(self, id, dto):
        usuario = self.get_by_id(id)

        if not usuario:
            raise ValueError("Usuario no encontrado.")

        if dto.nombre:
            usuario.nombre = dto.nombre

        if dto.correo:
            # Validar que no exista otro con ese correo
            existente = self.get_by_correo(dto.correo)
            if existente and existente.id_usuario != id:
                raise ValueError("El correo ya está en uso.")
            usuario.correo = dto.correo

        if dto.telefono:
            usuario.telefono = dto.telefono

        if dto.password:
            usuario.password_hash = bcrypt.generate_password_hash(dto.password).decode("utf-8")

        if dto.activo is not None:
            usuario.activo = dto.activo

        db.session.commit()
        return usuario

    def eliminar(self, id):
        usuario = self.get_by_id(id)

        if not usuario:
            raise ValueError("Usuario no encontrado.")

        usuario.activo = False  # Soft delete
        db.session.commit()