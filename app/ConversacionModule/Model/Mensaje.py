from app import db
from datetime import datetime

class Mensaje(db.Model):
    __tablename__ = "mensajes"

    id_mensaje       = db.Column(db.Integer, primary_key=True)
    id_conversacion  = db.Column(db.Integer, db.ForeignKey("conversaciones.id_conversacion"), nullable=False)
    id_emisor        = db.Column(db.Integer, db.ForeignKey("usuarios.id_usuario"))

    mensaje = db.Column(db.Text)
    tipo    = db.Column(db.Enum("TEXTO","IMAGEN", name="tipo_mensaje_enum"), default="TEXTO")
    fecha_envio = db.Column(db.DateTime, default=datetime.utcnow)