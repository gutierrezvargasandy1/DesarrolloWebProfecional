from app import db
from app.MascotaModule.Model.Mascota import Mascota
from app.utils.FileService import FileService


class MascotaService:


    # ─────────────────────────────────────
    # CREAR MASCOTA
    # ─────────────────────────────────────
    def crear_mascota(self, data, file):

        foto_url = None

        if file:
            nombre_archivo = FileService.guardar_imagen(file)
            foto_url = f"/files/uploads/{nombre_archivo}"
        mascota = Mascota(
            id_usuario       = data["id_usuario"],
            nombre           = data.get("nombre"),
            especie          = data["especie"],
            raza             = data.get("raza"),
            color            = data.get("color"),
            sexo             = data.get("sexo"),
            edad             = data.get("edad"),
            descripcion      = data.get("descripcion"),
            foto_url         = foto_url,

            # NUEVO CAMPO
            estado           = data.get("estado", "NORMAL"),

            direccion_hogar  = data.get("direccion_hogar"),
            latitud_hogar    = data.get("latitud_hogar"),
            longitud_hogar   = data.get("longitud_hogar")
        )

        db.session.add(mascota)
        db.session.commit()

        return mascota


    # ─────────────────────────────────────
    # OBTENER TODAS LAS MASCOTAS
    # ─────────────────────────────────────
    def obtener_mascotas(self):

        mascotas = Mascota.query.all()

        return mascotas


    # ─────────────────────────────────────
    # OBTENER MASCOTA POR ID
    # ─────────────────────────────────────
    def obtener_mascota(self, id_mascota):

        mascota = Mascota.query.get(id_mascota)

        if not mascota:
            raise ValueError("Mascota no encontrada.")

        return mascota


    # ─────────────────────────────────────
    # ACTUALIZAR MASCOTA
    # ─────────────────────────────────────
    def actualizar_mascota(self, id_mascota, data, file):

        mascota = Mascota.query.get(id_mascota)

        if not mascota:
            raise ValueError("Mascota no encontrada.")

        mascota.nombre          = data.get("nombre", mascota.nombre)
        mascota.especie         = data.get("especie", mascota.especie)
        mascota.raza            = data.get("raza", mascota.raza)
        mascota.color           = data.get("color", mascota.color)
        mascota.sexo            = data.get("sexo", mascota.sexo)
        mascota.edad            = data.get("edad", mascota.edad)
        mascota.descripcion     = data.get("descripcion", mascota.descripcion)

        # NUEVO CAMPO
        mascota.estado          = data.get("estado", mascota.estado)

        mascota.direccion_hogar = data.get("direccion_hogar", mascota.direccion_hogar)
        mascota.latitud_hogar   = data.get("latitud_hogar", mascota.latitud_hogar)
        mascota.longitud_hogar  = data.get("longitud_hogar", mascota.longitud_hogar)

        # actualizar imagen
        if file:

            if mascota.foto_url:
                nombre_viejo = mascota.foto_url.split("/")[-1]
                FileService.eliminar_imagen(nombre_viejo)

            nombre_archivo = FileService.guardar_imagen(file)
            mascota.foto_url = f"/files/uploads/{nombre_archivo}"

        db.session.commit()

        return mascota


    # ─────────────────────────────────────
    # ELIMINAR MASCOTA
    # ─────────────────────────────────────
    def eliminar_mascota(self, id_mascota):

        mascota = Mascota.query.get(id_mascota)

        if not mascota:
            raise ValueError("Mascota no encontrada.")

        if mascota.foto_url:
            nombre_archivo = mascota.foto_url.split("/")[-1]
            FileService.eliminar_imagen(nombre_archivo)

        db.session.delete(mascota)
        db.session.commit()

        return True