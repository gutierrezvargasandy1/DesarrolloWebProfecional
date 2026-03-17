from app import db
from app.ReporteModule.Model.ReporteMascota import ReporteMascota
from app.MascotaModule.Model.Mascota import Mascota
from app.ReporteModule.Dto.MascotaReporteDTO import MascotaReporteDTO


class ReporteMascotaService:

    # ✅ OBTENER TODOS LOS REPORTES
    def get_all(self):
        return ReporteMascota.query.all()

    # ✅ OBTENER POR ID
    def get_by_id(self, id):
        return db.session.get(ReporteMascota, id)

    # ✅ CREAR REPORTE (DTO)
    def crear(self, dto):
        # validar que exista la mascota
        mascota = db.session.get(Mascota, dto.id_mascota)
        if not mascota:
            raise ValueError("La mascota no existe.")

        reporte = ReporteMascota(
            id_mascota = dto.id_mascota,
            id_usuario = dto.id_usuario,
            descripcion = dto.descripcion,
            latitud = dto.latitud,
            longitud = dto.longitud,
            direccion = dto.direccion,
            estado = dto.estado
        )

        db.session.add(reporte)
        db.session.commit()
        return reporte

    # ✅ ACTUALIZAR REPORTE (DTO)
    def actualizar(self, id, dto):
        reporte = self.get_by_id(id)

        if not reporte:
            raise ValueError("Reporte no encontrado.")

        if dto.descripcion:
            reporte.descripcion = dto.descripcion

        if dto.latitud:
            reporte.latitud = dto.latitud

        if dto.longitud:
            reporte.longitud = dto.longitud

        if dto.direccion:
            reporte.direccion = dto.direccion

        if dto.estado:
            reporte.estado = dto.estado

        db.session.commit()
        return reporte

    # ✅ ELIMINAR REPORTE (hard delete)
    def eliminar(self, id):
        reporte = self.get_by_id(id)

        if not reporte:
            raise ValueError("Reporte no encontrado.")

        db.session.delete(reporte)
        db.session.commit()

    # 🔥 OBTENER REPORTE POR ID + MASCOTA
    def get_by_id_with_mascota(self, id):
        reporte = ReporteMascota.query.filter_by(id_reporte=id).first()

        if not reporte:
            raise ValueError("Reporte no encontrado.")

        dto = MascotaReporteDTO(reporte.mascota, reporte)
        return dto.to_dict()

    # 🔥 OBTENER TODOS LOS REPORTES + MASCOTAS
    def get_all_with_mascota(self):
        reportes = ReporteMascota.query.all()

        resultado = []
        for r in reportes:
            dto = MascotaReporteDTO(r.mascota, r)
            resultado.append(dto.to_dict())

        return resultado