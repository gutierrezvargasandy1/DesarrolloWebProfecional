from sqlalchemy import Column, Integer, String, DateTime, Numeric, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app import db


class Avistamiento(db.Model):
    __tablename__ = "avistamientos"

    id_avistamiento    = Column(Integer, primary_key=True, autoincrement=True)
    id_reporte         = Column(Integer, ForeignKey("reportesmascota.id_reporte"), nullable=False)
    id_usuario         = Column(Integer, ForeignKey("usuarios.id_usuario"), nullable=False)
    descripcion        = Column(String(255), nullable=True)
    latitud            = Column(Numeric(10, 7), nullable=False)
    longitud           = Column(Numeric(10, 7), nullable=False)
    foto_url           = Column(String(500), nullable=True)
    fecha_avistamiento = Column(DateTime, default=func.now())

    # Relaciones
    reporte = relationship("ReporteMascota", back_populates="avistamientos")
    usuario = relationship("Usuario", back_populates="avistamientos", foreign_keys=[id_usuario])