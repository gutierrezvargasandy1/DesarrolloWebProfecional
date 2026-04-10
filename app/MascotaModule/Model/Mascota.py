from app import db
from datetime import datetime
from sqlalchemy import Enum

class Mascota(db.Model):
    __tablename__ = "mascotas"

    id_mascota = db.Column(db.Integer, primary_key=True)
    id_usuario = db.Column(
        db.Integer,
        db.ForeignKey("usuarios.id_usuario"),
        nullable=False
    )

    nombre = db.Column(db.String(100))
    especie = db.Column(db.String(50), nullable=False)
    raza = db.Column(db.String(100))
    color = db.Column(db.String(100))
    sexo = db.Column(db.String(20))
    edad = db.Column(db.Integer)

    descripcion = db.Column(db.Text)
    foto_url = db.Column(db.String(300))

    # NUEVO CAMPO
    estado = db.Column(
        Enum(
            "PERDIDA",
            "ENCONTRADA",
            "NORMAL",
            "CELO",
            "ADOPCION",
            name="estado_mascota_enum"
        ),
        default="NORMAL"
    )

    direccion_hogar = db.Column(db.String(250))
    latitud_hogar = db.Column(db.Numeric(10, 8))
    longitud_hogar = db.Column(db.Numeric(11, 8))

    fecha_registro = db.Column(db.DateTime, default=datetime.utcnow)

    reportes = db.relationship(
        "ReporteMascota",
        backref="mascota",
        lazy=True,
        cascade="all, delete-orphan"
    )