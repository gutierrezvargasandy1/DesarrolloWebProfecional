from app import db
from app.UsuarioModule.Model.Usuario import Usuario
from flask_bcrypt import Bcrypt

bcrypt = Bcrypt()


class UsuarioService:

    # ─────────────────────────────────────
    # OBTENER USUARIO POR ID (del JWT)
    # ─────────────────────────────────────
    def get_by_id(self, user_id: int) -> Usuario:
        usuario = db.session.get(Usuario, user_id)

        if not usuario or not usuario.activo:
            raise ValueError("Usuario no encontrado.")

        return usuario

    # ─────────────────────────────────────
    # OBTENER POR CORREO
    # ─────────────────────────────────────
    def get_by_correo(self, correo: str):
        return Usuario.query.filter_by(correo=correo).first()

    # ─────────────────────────────────────
    # CREAR USUARIO (REGISTRO)
    # ─────────────────────────────────────
    def crear(self, dto):
        if self.get_by_correo(dto.correo):
            raise ValueError("El correo ya está registrado.")

        usuario = Usuario(
            nombre=dto.nombre,
            correo=dto.correo,
            password_hash=bcrypt.generate_password_hash(dto.password).decode("utf-8"),
            telefono=dto.telefono,
            activo=True
        )

        db.session.add(usuario)
        db.session.commit()
        return usuario

    # ─────────────────────────────────────
    # ACTUALIZAR USUARIO (DEL JWT)
    # ─────────────────────────────────────
    def actualizar(self, user_id: int, dto):
        usuario = self.get_by_id(user_id)

        if dto.nombre:
            usuario.nombre = dto.nombre

        if dto.correo:
            existente = self.get_by_correo(dto.correo)
            if existente and existente.id_usuario != user_id:
                raise ValueError("El correo ya está en uso.")
            usuario.correo = dto.correo

        if dto.telefono:
            usuario.telefono = dto.telefono

        if dto.password:
            usuario.password_hash = bcrypt.generate_password_hash(dto.password).decode("utf-8")



        db.session.commit()
        return usuario

    # ─────────────────────────────────────
    # ELIMINAR USUARIO (SOFT DELETE DEL JWT)
    # ─────────────────────────────────────
    def eliminar(self, user_id: int):
        usuario = self.get_by_id(user_id)

        usuario.activo = False
        db.session.commit()