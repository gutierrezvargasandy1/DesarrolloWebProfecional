from app import db
from datetime import datetime

class Avistamiento(db.Model):
    __tablename__ = "avistamientos"

    id_avistamiento = db.Column(db.Integer, primary_key=True)
    id_reporte      = db.Column(db.Integer, db.ForeignKey("reportesmascota.id_reporte"), nullable=False)
    id_usuario      = db.Column(db.Integer, db.ForeignKey("usuarios.id_usuario"))

    descripcion      = db.Column(db.Text)
    latitud          = db.Column(db.Numeric(10,8))
    longitud         = db.Column(db.Numeric(11,8))
    foto_url         = db.Column(db.String(300))
    fecha_avistamiento = db.Column(db.DateTime, default=datetime.utcnow)