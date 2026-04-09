from app import db
from app.AvistamientoModule.Model.Avistamiento import Avistamiento
from app.AvistamientoModule.Dto.AvistamientoDTO import AvistamientoDTO


class AvistamientoService:

    # ── OBTENER TODOS ──────────────────────────────────────
    def obtener_todos(self):
        avistamientos = (
            Avistamiento.query
            .order_by(Avistamiento.fecha_avistamiento.desc())
            .all()
        )
        return [AvistamientoDTO(a).to_dict() for a in avistamientos]

    # ── OBTENER POR ID ─────────────────────────────────────
    def obtener_por_id(self, id_avistamiento):
        avistamiento = Avistamiento.query.get(id_avistamiento)
        if not avistamiento:
            return None
        return AvistamientoDTO(
            avistamiento,
            usuario=avistamiento.usuario,
            reporte=avistamiento.reporte
        ).to_dict()

    # ── OBTENER POR REPORTE ────────────────────────────────
    def obtener_por_reporte(self, id_reporte):
        avistamientos = (
            Avistamiento.query
            .filter_by(id_reporte=id_reporte)
            .order_by(Avistamiento.fecha_avistamiento.desc())
            .all()
        )
        return [AvistamientoDTO(a, usuario=a.usuario).to_dict() for a in avistamientos]

    # ── OBTENER POR USUARIO ────────────────────────────────
    def obtener_por_usuario(self, id_usuario):
        avistamientos = (
            Avistamiento.query
            .filter_by(id_usuario=id_usuario)
            .order_by(Avistamiento.fecha_avistamiento.desc())
            .all()
        )
        return [AvistamientoDTO(a, reporte=a.reporte).to_dict() for a in avistamientos]

    # ── CREAR ──────────────────────────────────────────────
    def crear(self, data: dict):
        for campo in ["id_reporte", "id_usuario", "latitud", "longitud"]:
            if not data.get(campo):
                raise ValueError(f"El campo '{campo}' es requerido")

        nuevo = Avistamiento(
            id_reporte  = data["id_reporte"],
            id_usuario  = data["id_usuario"],
            descripcion = data.get("descripcion"),
            latitud     = data["latitud"],
            longitud    = data["longitud"],
            foto_url    = data.get("foto_url")
        )
        db.session.add(nuevo)
        db.session.commit()
        return AvistamientoDTO(nuevo).to_dict()

    # ── ACTUALIZAR ─────────────────────────────────────────
    def actualizar(self, id_avistamiento, data: dict):
        avistamiento = Avistamiento.query.get(id_avistamiento)
        if not avistamiento:
            raise ValueError("Avistamiento no encontrado")

        for campo in ["descripcion", "foto_url", "latitud", "longitud"]:
            if campo in data:
                setattr(avistamiento, campo, data[campo])

        db.session.commit()
        return AvistamientoDTO(avistamiento).to_dict()

    # ── ELIMINAR ───────────────────────────────────────────
    def eliminar(self, id_avistamiento):
        avistamiento = Avistamiento.query.get(id_avistamiento)
        if not avistamiento:
            raise ValueError("Avistamiento no encontrado")

        db.session.delete(avistamiento)
        db.session.commit()
        return {"mensaje": "Avistamiento eliminado correctamente"}