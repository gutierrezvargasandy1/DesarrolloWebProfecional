from app import db
from datetime import datetime

class ReporteMascota(db.Model):
    __tablename__ = "reportesmascota"

    id_reporte = db.Column(db.Integer, primary_key=True)
    id_mascota = db.Column(db.Integer, db.ForeignKey("mascotas.id_mascota"), nullable=False)
    id_usuario = db.Column(db.Integer, db.ForeignKey("usuarios.id_usuario"), nullable=False)

    descripcion = db.Column(db.Text)
    latitud     = db.Column(db.Numeric(10,8), nullable=False)
    longitud    = db.Column(db.Numeric(11,8), nullable=False)
    direccion   = db.Column(db.String(250))
    estado      = db.Column(db.Enum("PERDIDA","ENCONTRADA","CERRADA", name="estado_mascota_enum"), default="PERDIDA")
    fecha_reporte = db.Column(db.DateTime, default=datetime.utcnow)

    avistamientos = db.relationship(
        "Avistamiento",
        backref="reporte",
        lazy=True,
        cascade="all, delete-orphan"
    )
    conversaciones = db.relationship(
        "Conversacion",
        backref="reporte",
        lazy=True,
        cascade="all, delete-orphan"
    )