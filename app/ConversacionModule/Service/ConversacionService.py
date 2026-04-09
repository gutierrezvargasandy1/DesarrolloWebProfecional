from app import db
from app.ConversacionModule.Model.Conversacion import Conversacion, Mensaje
from app.ConversacionModule.Dto.ConversacionDTO import ConversacionDTO, MensajeDTO


class ConversacionService:

    # ── CONVERSACIONES ─────────────────────────────────────

    def obtener_conversaciones_usuario(self, id_usuario):
        conversaciones = (
            Conversacion.query
            .filter(
                (Conversacion.id_usuario_1 == id_usuario) |
                (Conversacion.id_usuario_2 == id_usuario)
            ).all()
        )
        return [ConversacionDTO(c).to_dict() for c in conversaciones]

    def obtener_conversacion_por_id(self, id_conversacion):
        conversacion = Conversacion.query.get(id_conversacion)
        if not conversacion:
            return None
        mensajes = (
            Mensaje.query
            .filter_by(id_conversacion=id_conversacion)
            .order_by(Mensaje.fecha_envio.asc())
            .all()
        )
        return ConversacionDTO(conversacion, mensajes).to_dict()

    def crear_conversacion(self, id_reporte, id_usuario_1, id_usuario_2):
        existente = Conversacion.query.filter_by(id_reporte=id_reporte).filter(
            ((Conversacion.id_usuario_1 == id_usuario_1) & (Conversacion.id_usuario_2 == id_usuario_2)) |
            ((Conversacion.id_usuario_1 == id_usuario_2) & (Conversacion.id_usuario_2 == id_usuario_1))
        ).first()

        if existente:
            raise ValueError("Ya existe una conversación para este reporte entre estos usuarios")

        nueva = Conversacion(
            id_reporte=id_reporte,
            id_usuario_1=id_usuario_1,
            id_usuario_2=id_usuario_2
        )
        db.session.add(nueva)
        db.session.commit()
        return ConversacionDTO(nueva).to_dict()

    # ── MENSAJES ───────────────────────────────────────────

    def enviar_mensaje(self, id_conversacion, id_emisor, mensaje, tipo="texto"):
        conversacion = Conversacion.query.get(id_conversacion)
        if not conversacion:
            raise ValueError("Conversación no encontrada")

        if id_emisor not in (conversacion.id_usuario_1, conversacion.id_usuario_2):
            raise PermissionError("No tienes permiso para enviar mensajes en esta conversación")

        nuevo = Mensaje(
            id_conversacion=id_conversacion,
            id_emisor=id_emisor,
            mensaje=mensaje,
            tipo=tipo
        )
        db.session.add(nuevo)
        db.session.commit()
        return MensajeDTO(nuevo).to_dict()

    def obtener_mensajes(self, id_conversacion):
        mensajes = (
            Mensaje.query
            .filter_by(id_conversacion=id_conversacion)
            .order_by(Mensaje.fecha_envio.asc())
            .all()
        )
        return [MensajeDTO(m).to_dict() for m in mensajes]