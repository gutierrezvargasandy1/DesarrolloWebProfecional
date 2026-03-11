import os
from werkzeug.utils import secure_filename
from uuid import uuid4

UPLOAD_FOLDER = "uploads/imagenes"

class FileService:

    @staticmethod
    def guardar_imagen(file):

        if not os.path.exists(UPLOAD_FOLDER):
            os.makedirs(UPLOAD_FOLDER)

        extension = file.filename.split(".")[-1]

        nombre_archivo = f"{uuid4()}.{extension}"

        ruta = os.path.join(UPLOAD_FOLDER, secure_filename(nombre_archivo))

        file.save(ruta)

        return nombre_archivo


    @staticmethod
    def eliminar_imagen(nombre_archivo):

        ruta = os.path.join(UPLOAD_FOLDER, nombre_archivo)

        if not os.path.exists(ruta):
            raise ValueError("El archivo no existe.")

        os.remove(ruta)

        return True