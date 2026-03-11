from app import db
from datetime import datetime

class Conversacion(db.Model):
    __tablename__ = "conversaciones"

    id_conversacion = db.Column(db.Integer, primary_key=True)
    id_reporte      = db.Column(db.Integer, db.ForeignKey("reportesmascota.id_reporte"), nullable=False)
    id_usuario1     = db.Column(db.Integer, db.ForeignKey("usuarios.id_usuario"))
    id_usuario2     = db.Column(db.Integer, db.ForeignKey("usuarios.id_usuario"))
    fecha_inicio    = db.Column(db.DateTime, default=datetime.utcnow)

    mensajes = db.relationship(
        "Mensaje",
        backref="conversacion",
        lazy=True,
        cascade="all, delete-orphan"
    )