from app import db
from datetime import datetime


class Usuario(db.Model):
    __tablename__ = "usuarios"

    id_usuario         = db.Column(db.Integer, primary_key=True)
    nombre             = db.Column(db.String(100), nullable=False)
    correo             = db.Column(db.String(150), unique=True, nullable=False)
    password_hash      = db.Column(db.String(255), nullable=False)
    telefono           = db.Column(db.String(20))
    token_recuperacion = db.Column(db.String(255))
    expiracion_token   = db.Column(db.DateTime)
    fecha_registro     = db.Column(db.DateTime, default=datetime.utcnow)
    activo             = db.Column(db.Boolean, default=True)

    # Relaciones
    mascotas = db.relationship(
        "Mascota",
        backref="usuario",
        lazy=True,
        cascade="all, delete-orphan"
    )
    reportes = db.relationship(
        "ReporteMascota",
        backref="usuario",
        lazy=True,
        cascade="all, delete-orphan"
    )
    avistamientos = db.relationship(
        "Avistamiento",
        back_populates="usuario",
        lazy=True
    )
    conversaciones1 = db.relationship(
        "Conversacion",
        back_populates="usuario_1",
        foreign_keys="Conversacion.id_usuario_1",
        lazy=True
    )
    conversaciones2 = db.relationship(
        "Conversacion",
        back_populates="usuario_2",
        foreign_keys="Conversacion.id_usuario_2",
        lazy=True
    )
    mensajes = db.relationship(
        "Mensaje",
        back_populates="emisor",         # ← back_populates en lugar de backref
        foreign_keys="Mensaje.id_emisor",
        lazy=True
    )

    def to_dict(self):
        return {
            "id_usuario":      self.id_usuario,
            "nombre":          self.nombre,
            "correo":          self.correo,
            "telefono":        self.telefono,
            "fecha_registro":  self.fecha_registro.isoformat(),
            "activo":          self.activo
        }