class ConversacionDTO:
    def __init__(self, conversacion, mensajes=None):
        self.id_conversacion = conversacion.id_conversacion
        self.id_reporte = conversacion.id_reporte
        self.id_usuario_1 = conversacion.id_usuario_1
        self.id_usuario_2 = conversacion.id_usuario_2
        self.fecha_inicio = conversacion.fecha_inicio
        self.mensajes = []

        if mensajes:
            self.mensajes = [
                {
                    "id_mensaje": m.id_mensaje,
                    "id_emisor": m.id_emisor,
                    "mensaje": m.mensaje,
                    "tipo": m.tipo,
                    "fecha_envio": m.fecha_envio.isoformat() if m.fecha_envio else None
                }
                for m in mensajes
            ]

    def to_dict(self):
        return {
            "id_conversacion": self.id_conversacion,
            "id_reporte": self.id_reporte,
            "id_usuario_1": self.id_usuario_1,
            "id_usuario_2": self.id_usuario_2,
            "fecha_inicio": self.fecha_inicio.isoformat() if self.fecha_inicio else None,
            "mensajes": self.mensajes
        }


class MensajeDTO:
    def __init__(self, mensaje):
        self.id_mensaje = mensaje.id_mensaje
        self.id_conversacion = mensaje.id_conversacion
        self.id_emisor = mensaje.id_emisor
        self.mensaje = mensaje.mensaje
        self.tipo = mensaje.tipo
        self.fecha_envio = mensaje.fecha_envio

    def to_dict(self):
        return {
            "id_mensaje": self.id_mensaje,
            "id_conversacion": self.id_conversacion,
            "id_emisor": self.id_emisor,
            "mensaje": self.mensaje,
            "tipo": self.tipo,
            "fecha_envio": self.fecha_envio.isoformat() if self.fecha_envio else None
        }