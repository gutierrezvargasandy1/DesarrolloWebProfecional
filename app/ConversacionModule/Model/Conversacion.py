from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app import db


class Conversacion(db.Model):
    __tablename__ = "conversaciones"

    id_conversacion = Column(Integer, primary_key=True, autoincrement=True)
    id_reporte      = Column(Integer, ForeignKey("reportesmascota.id_reporte"), nullable=False)
    id_usuario_1    = Column(Integer, ForeignKey("usuarios.id_usuario"), nullable=False)
    id_usuario_2    = Column(Integer, ForeignKey("usuarios.id_usuario"), nullable=False)
    fecha_inicio    = Column(DateTime, default=func.now())

    # Relaciones
    reporte   = relationship("ReporteMascota", back_populates="conversaciones")
    usuario_1 = relationship("Usuario", back_populates="conversaciones1", foreign_keys=[id_usuario_1])
    usuario_2 = relationship("Usuario", back_populates="conversaciones2", foreign_keys=[id_usuario_2])
    mensajes  = relationship("Mensaje", back_populates="conversacion", cascade="all, delete-orphan")


class Mensaje(db.Model):
    __tablename__ = "mensajes"

    id_mensaje      = Column(Integer, primary_key=True, autoincrement=True)
    id_conversacion = Column(Integer, ForeignKey("conversaciones.id_conversacion"), nullable=False)
    id_emisor       = Column(Integer, ForeignKey("usuarios.id_usuario"), nullable=False)
    mensaje         = Column(Text, nullable=False)
    tipo            = Column(String(50), default="texto")
    fecha_envio     = Column(DateTime, default=func.now())

    # Relaciones
    conversacion = relationship("Conversacion", back_populates="mensajes")
    emisor       = relationship("Usuario", back_populates="mensajes", foreign_keys=[id_emisor])  # ← back_populates