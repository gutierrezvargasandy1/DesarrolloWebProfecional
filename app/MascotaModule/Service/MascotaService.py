from app import db
from app.MascotaModule.Model.Mascota import Mascota
from app.utils.FileService import FileService


class MascotaService:

    def crear_mascota(self, user_id, data, file):

        foto_url = None

        if file:
            nombre_archivo = FileService.guardar_imagen(file)
            foto_url = f"/files/uploads/{nombre_archivo}"

        mascota = Mascota(
            id_usuario=user_id,
            foto_url=foto_url,
            **data
        )

        db.session.add(mascota)
        db.session.commit()

        return mascota

    # 🔥 SOLO mascotas del usuario
    def obtener_mascotas_por_usuario(self, user_id):
        return Mascota.query.filter_by(id_usuario=user_id).all()


    # 🔥 Valida dueño
    def actualizar_mascota_usuario(self, id_mascota, user_id, data, file):

        mascota = self.obtener_mascota_usuario(id_mascota, user_id)

        for key, value in data.items():
            if value is not None:
                setattr(mascota, key, value)

        if file:
            if mascota.foto_url:
                nombre_viejo = mascota.foto_url.split("/")[-1]
                FileService.eliminar_imagen(nombre_viejo)

            nombre_archivo = FileService.guardar_imagen(file)
            mascota.foto_url = f"/files/uploads/{nombre_archivo}"

        db.session.commit()
        return mascota

    # 🔥 Valida dueño
    def eliminar_mascota_usuario(self, id_mascota, user_id):

        mascota = self.obtener_mascota_usuario(id_mascota, user_id)

        if mascota.foto_url:
            nombre_archivo = mascota.foto_url.split("/")[-1]
            FileService.eliminar_imagen(nombre_archivo)

        db.session.delete(mascota)
        db.session.commit()

        return True