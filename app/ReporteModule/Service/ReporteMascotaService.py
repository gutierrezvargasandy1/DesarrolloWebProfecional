from app import db
from app.ReporteModule.Model.ReporteMascota import ReporteMascota
from app.MascotaModule.Model.Mascota import Mascota
from app.ReporteModule.Model.Avistamiento import Avistamiento


class ReporteMascotaService:

    """
    Servicio encargado de toda la lógica de negocio relacionada a los reportes de mascotas,
    incluyendo consultas privadas, públicas y el registro de avistamientos.
    """

    # ================= PRIVADOS POR USUARIO =================

    def get_all_by_user(self, user_id):
        """
        Obtiene todos los reportes creados por un usuario específico.

        :param user_id: ID del usuario autenticado.
        :return: Lista de objetos ReporteMascota pertenecientes al usuario.
        """
        return ReporteMascota.query.filter_by(id_usuario=user_id).all()

    def get_by_id_and_user(self, id_reporte, user_id):
        """
        Obtiene un reporte específico validando que pertenezca al usuario.

        :param id_reporte: ID del reporte.
        :param user_id: ID del usuario autenticado.
        :return: Objeto ReporteMascota.
        :raises ValueError: Si el reporte no existe o no pertenece al usuario.
        """
        reporte = ReporteMascota.query.filter_by(
            id_reporte=id_reporte,
            id_usuario=user_id
        ).first()

        if not reporte:
            raise ValueError("Reporte no encontrado.")

        return reporte

    # ================= CREAR =================

    def crear(self, dto, user_id):
        """
        Crea un nuevo reporte de mascota validando que la mascota exista
        y pertenezca al usuario autenticado.

        :param dto: DTO con la información del reporte.
        :param user_id: ID del usuario autenticado.
        :return: ReporteMascota creado.
        :raises ValueError: Si la mascota no existe o no pertenece al usuario.
        """
        mascota = db.session.get(Mascota, dto.id_mascota)

        if not mascota:
            raise ValueError("La mascota no existe.")

        if mascota.id_usuario != user_id:
            raise ValueError("No puedes reportar mascotas que no son tuyas.")

        reporte = ReporteMascota(
            id_mascota=dto.id_mascota,
            id_usuario=user_id,
            descripcion=dto.descripcion,
            latitud=dto.latitud,
            longitud=dto.longitud,
            direccion=dto.direccion,
            estado=dto.estado
        )

        db.session.add(reporte)
        db.session.commit()
        return reporte

    # ================= ACTUALIZAR =================

    def actualizar(self, id_reporte, dto, user_id):
        """
        Actualiza parcialmente un reporte del usuario.

        Solo se actualizan los campos enviados en el DTO.

        :param id_reporte: ID del reporte a actualizar.
        :param dto: DTO con los nuevos valores.
        :param user_id: ID del usuario autenticado.
        :return: ReporteMascota actualizado.
        """
        reporte = self.get_by_id_and_user(id_reporte, user_id)

        if dto.descripcion is not None:
            reporte.descripcion = dto.descripcion
        if dto.latitud is not None:
            reporte.latitud = dto.latitud
        if dto.longitud is not None:
            reporte.longitud = dto.longitud
        if dto.direccion is not None:
            reporte.direccion = dto.direccion
        if dto.estado is not None:
            reporte.estado = dto.estado

        db.session.commit()
        return reporte

    # ================= ELIMINAR =================

    def eliminar(self, id_reporte, user_id):
        """
        Elimina un reporte que pertenece al usuario autenticado.

        :param id_reporte: ID del reporte.
        :param user_id: ID del usuario autenticado.
        """
        reporte = self.get_by_id_and_user(id_reporte, user_id)
        db.session.delete(reporte)
        db.session.commit()

    # ================= CONSULTAS PRIVADAS COMPLETAS =================

    def get_all_with_mascota_by_user(self, user_id):
        """
        Obtiene todos los reportes del usuario incluyendo
        información de la mascota y avistamientos.

        :param user_id: ID del usuario autenticado.
        :return: Lista de diccionarios con la información completa.
        """
        reportes = self.get_all_by_user(user_id)
        return [self._map_reporte_completo(r) for r in reportes]

    def get_by_id_with_mascota_and_user(self, id_reporte, user_id):
        """
        Obtiene un reporte específico del usuario con toda su información relacionada.

        :param id_reporte: ID del reporte.
        :param user_id: ID del usuario autenticado.
        :return: Diccionario con información completa del reporte.
        """
        reporte = self.get_by_id_and_user(id_reporte, user_id)
        return self._map_reporte_completo(reporte)

    # ================= CONSULTAS PÚBLICAS =================

    def get_all_public_with_mascota(self):
        """
        Obtiene todos los reportes del sistema sin necesidad de autenticación,
        incluyendo mascota y avistamientos.

        :return: Lista de reportes completos.
        """
        reportes = ReporteMascota.query.all()
        return [self._map_reporte_completo(r) for r in reportes]

    def get_by_id_public_with_mascota(self, id_reporte):
        """
        Obtiene un reporte específico de forma pública.

        :param id_reporte: ID del reporte.
        :return: Diccionario con información completa del reporte.
        :raises ValueError: Si el reporte no existe.
        """
        reporte = db.session.get(ReporteMascota, id_reporte)

        if not reporte:
            raise ValueError("Reporte no encontrado.")

        return self._map_reporte_completo(reporte)

    # ================= AVISTAMIENTOS =================

    def crear_avistamiento(self, id_reporte: int, user_id: int, data: dict):
        """
        Registra un nuevo avistamiento sobre un reporte cuando la mascota está en estado PERDIDA.

        :param id_reporte: ID del reporte.
        :param user_id: ID del usuario que reporta el avistamiento.
        :param data: Diccionario con descripción, ubicación y foto.
        :return: Diccionario con la información del avistamiento creado.
        :raises ValueError: Si el reporte no existe o no está en estado PERDIDA.
        """
        reporte = db.session.get(ReporteMascota, id_reporte)

        if not reporte:
            raise ValueError("Reporte no existe.")

        if reporte.estado != "PERDIDA":
            raise ValueError("Solo se pueden registrar avistamientos cuando la mascota está PERDIDA.")

        nuevo = Avistamiento(
            id_reporte=id_reporte,
            id_usuario=user_id,
            descripcion=data.get("descripcion"),
            latitud=data.get("latitud"),
            longitud=data.get("longitud"),
            foto_url=data.get("foto_url")
        )

        db.session.add(nuevo)
        db.session.commit()

        return {
            "id_avistamiento": nuevo.id_avistamiento,
            "descripcion": nuevo.descripcion,
            "latitud": float(nuevo.latitud) if nuevo.latitud else None,
            "longitud": float(nuevo.longitud) if nuevo.longitud else None,
            "foto_url": nuevo.foto_url,
            "fecha": nuevo.fecha_avistamiento.isoformat()
        }
    
    def actualizar_avistamiento(self, id_avistamiento: int, user_id: int, data: dict):
        """
        Permite que el usuario autenticado edite SU PROPIO avistamiento
        solo dentro de una ventana de 15 minutos desde su creación.

        :param id_avistamiento: ID del avistamiento.
        :param user_id: ID del usuario autenticado (desde el JWT).
        :param data: Campos editables: descripcion, latitud, longitud, foto_url.
        :return: Diccionario con el avistamiento actualizado.
        :raises ValueError: Si no existe, no es del usuario o ya pasó el tiempo permitido.
        """

        avistamiento = db.session.get(Avistamiento, id_avistamiento)

        if not avistamiento:
            raise ValueError("Avistamiento no encontrado.")

        # ✅ Validar que el avistamiento pertenece al usuario autenticado
        if avistamiento.id_usuario != user_id:
            raise ValueError("No puedes editar un avistamiento que no es tuyo.")

        # ✅ Validar ventana de edición (15 minutos)
        limite_edicion = avistamiento.fecha_avistamiento + timedelta(minutes=15)
        if datetime.utcnow() > limite_edicion:
            raise ValueError("El tiempo para editar este avistamiento ha expirado.")

        # ✅ Solo campos permitidos
        if "descripcion" in data:
            avistamiento.descripcion = data["descripcion"]

        if "latitud" in data:
            avistamiento.latitud = data["latitud"]

        if "longitud" in data:
            avistamiento.longitud = data["longitud"]

        if "foto_url" in data:
            avistamiento.foto_url = data["foto_url"]

        db.session.commit()

        return {
            "id_avistamiento": avistamiento.id_avistamiento,
            "descripcion": avistamiento.descripcion,
            "latitud": float(avistamiento.latitud) if avistamiento.latitud else None,
            "longitud": float(avistamiento.longitud) if avistamiento.longitud else None,
            "foto_url": avistamiento.foto_url,
            "fecha": avistamiento.fecha_avistamiento.isoformat()
        }
    # ================= MAPEADOR CENTRAL =================

    def _map_reporte_completo(self, reporte):
        """
        Convierte un objeto ReporteMascota en un diccionario con toda su información
        relacionada: mascota y avistamientos.

        :param reporte: Objeto ReporteMascota.
        :return: Diccionario serializable para respuesta JSON.
        """
        return {
            "id_reporte": reporte.id_reporte,
            "descripcion": reporte.descripcion,
            "estado": reporte.estado,
            "latitud": float(reporte.latitud) if reporte.latitud else None,
            "longitud": float(reporte.longitud) if reporte.longitud else None,
            "direccion": reporte.direccion,
            "fecha_reporte": reporte.fecha_reporte.isoformat(),

            "mascota": {
                "id_mascota": reporte.mascota.id_mascota,
                "nombre": reporte.mascota.nombre,
                "foto_url": reporte.mascota.foto_url
            },

            "avistamientos": [
                {
                    "id_avistamiento": a.id_avistamiento,
                    "descripcion": a.descripcion,
                    "latitud": float(a.latitud) if a.latitud else None,
                    "longitud": float(a.longitud) if a.longitud else None,
                    "foto_url": a.foto_url,
                    "fecha": a.fecha_avistamiento.isoformat(),
                    "usuario": {
                        "nombre": a.usuario.nombre,
                        "telefono": a.usuario.telefono
                    }
                }
                for a in reporte.avistamientos
            ]
        }