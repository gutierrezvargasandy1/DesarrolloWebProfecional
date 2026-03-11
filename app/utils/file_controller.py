from flask import Blueprint, current_app, request, send_from_directory
from app.utils.FileService import FileService
from app.config.Dto.ApiResponse import ApiResponse
import os

file_bp = Blueprint("files", __name__)


# ── SUBIR IMAGEN ─────────────────────────
@file_bp.route("/upload", methods=["POST"])
def subir_imagen():

    if "file" not in request.files:
        return ApiResponse(400, "No se envió archivo").to_response()

    file = request.files["file"]

    nombre = FileService.guardar_imagen(file)

    url = f"/files/uploads/{nombre}"

    return ApiResponse(
        200,
        "Imagen subida",
        {
            "archivo": nombre,
            "url": url
        }
    ).to_response()


# ── VER IMAGEN ─────────────────────────
@file_bp.route("/uploads/<filename>")
def obtener_imagen(filename):

    # Ruta real donde están las imágenes
    ruta = os.path.join(current_app.root_path, "..", "uploads", "imagenes")
    ruta = os.path.abspath(ruta)

    return send_from_directory(ruta, filename)


# ── ELIMINAR IMAGEN ─────────────────────
@file_bp.route("/delete/<filename>", methods=["DELETE"])
def eliminar_imagen(filename):

    try:
        FileService.eliminar_imagen(filename)

        return ApiResponse(
            200,
            "Imagen eliminada correctamente.",
            True
        ).to_response()

    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()