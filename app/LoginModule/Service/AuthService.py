# app/services/auth_service.py

import random
import string
from datetime import datetime, timedelta
from app import db
from app.UsuarioModule.Model.Usuario import Usuario
from flask_bcrypt import Bcrypt

bcrypt = Bcrypt()


class AuthService:

    # ── LOGIN ──────────────────────────────────────────────
    def login(self, correo: str, password: str) -> Usuario:
        usuario = Usuario.query.filter_by(correo=correo, activo=True).first()
        if not usuario or not bcrypt.check_password_hash(usuario.password_hash, password):
            raise ValueError("Credenciales inválidas.")
        return usuario


    # ── SOLICITAR RECUPERACIÓN ─────────────────────────────
    def solicitar_recuperacion(self, correo: str) -> str:
        usuario = Usuario.query.filter_by(correo=correo, activo=True).first()
        if not usuario:
            raise ValueError("Correo no encontrado.")

        codigo = self._generar_codigo()
        usuario.token_recuperacion = codigo
        usuario.expiracion_token   = datetime.utcnow() + timedelta(minutes=5)
        db.session.commit()

        return codigo


    # ── VERIFICAR CÓDIGO ───────────────────────────────────
    def verificar_codigo(self, correo: str, codigo: str) -> bool:
        usuario = Usuario.query.filter_by(correo=correo, activo=True).first()

        if not usuario:
            raise ValueError("Correo no encontrado.")
        
        if usuario.token_recuperacion != codigo:
            return False

        if datetime.utcnow() > usuario.expiracion_token:
            raise ValueError("El código ha expirado.")

        return True


    # ── CAMBIAR CONTRASEÑA ─────────────────────────────────
    def cambiar_password(self, correo: str, codigo: str, nueva_password: str) -> bool:
        if not self.verificar_codigo(correo, codigo):
            raise ValueError("Código inválido.")

        usuario = Usuario.query.filter_by(correo=correo).first()
        usuario.password_hash      = bcrypt.generate_password_hash(nueva_password).decode("utf-8")
        usuario.token_recuperacion = None
        usuario.expiracion_token   = None
        db.session.commit()

        return True


    # ── HELPER: generar código ─────────────────────────────
    def _generar_codigo(self, longitud: int = 6) -> str:
        return "".join(random.choices(string.digits, k=longitud))