from flask import Blueprint, request
from app.MascotaModule.Service.MascotaService import MascotaService
from app.MascotaModule.Dto.CreateMascotaDto import CreateMascotaDto
from app.MascotaModule.Dto.UpdateMascotaDto import UpdateMascotaDto
from app.config.Dto.ApiResponse import ApiResponse
from app.config.JwtFilter import jwt_required

mascota_bp = Blueprint("mascotas", __name__)
mascota_service = MascotaService()


# ─────────────────────────────────────
# CREAR MASCOTA
# ─────────────────────────────────────
@mascota_bp.route("/", methods=["POST"])
@jwt_required
def crear_mascota():
    """
    Crear mascota
    ---
    tags:
      - Mascotas
    description: Registrar una mascota con foto.
    consumes:
      - multipart/form-data
    parameters:

      - name: Authorization
        in: header
        type: string
        required: true
        description: Bearer JWT Token

      - name: id_usuario
        in: formData
        type: integer
        required: true
        example: 1

      - name: nombre
        in: formData
        type: string
        example: Firulais

      - name: especie
        in: formData
        type: string
        required: true
        example: Perro

      - name: raza
        in: formData
        type: string
        example: Labrador

      - name: color
        in: formData
        type: string
        example: Negro

      - name: sexo
        in: formData
        type: string
        example: Macho

      - name: edad
        in: formData
        type: integer
        example: 3

      - name: estado
        in: formData
        type: string
        example: NORMAL
        description: Estado de la mascota (NORMAL, PERDIDA, ENCONTRADA, CELO)

      - name: descripcion
        in: formData
        type: string

      - name: direccion_hogar
        in: formData
        type: string
        example: Calle Hidalgo 123

      - name: latitud_hogar
        in: formData
        type: number
        example: 20.5234

      - name: longitud_hogar
        in: formData
        type: number
        example: -100.8123

      - name: foto
        in: formData
        type: file

    responses:
      201:
        description: Mascota creada correctamente
      400:
        description: Error en los datos
    """

    try:

        data = CreateMascotaDto.from_request(request.form.to_dict())
        file = request.files.get("foto")

        mascota = mascota_service.crear_mascota(data, file)

        return ApiResponse(201, "Mascota creada correctamente.", {
            "id_mascota": mascota.id_mascota
        }).to_response()

    except ValueError as e:
        return ApiResponse(400, str(e)).to_response()


# ─────────────────────────────────────
# OBTENER TODAS LAS MASCOTAS
# ─────────────────────────────────────
@mascota_bp.route("/", methods=["GET"])
@jwt_required
def obtener_mascotas():

    """
    Obtener todas las mascotas
    ---
    tags:
      - Mascotas
    description: Lista todas las mascotas registradas.
    parameters:

      - name: Authorization
        in: header
        type: string
        required: true
        description: Bearer JWT Token

    responses:
      200:
        description: Lista de mascotas
    """

    mascotas = mascota_service.obtener_mascotas()

    data = []

    for m in mascotas:
        data.append({
            "id_mascota": m.id_mascota,
            "id_usuario": m.id_usuario,
            "nombre": m.nombre,
            "especie": m.especie,
            "raza": m.raza,
            "color": m.color,
            "sexo": m.sexo,
            "edad": m.edad,
            "estado": m.estado,
            "descripcion": m.descripcion,
            "foto_url": m.foto_url,
            "direccion_hogar": m.direccion_hogar,
            "latitud_hogar": float(m.latitud_hogar) if m.latitud_hogar else None,
            "longitud_hogar": float(m.longitud_hogar) if m.longitud_hogar else None,
            "fecha_registro": m.fecha_registro
        })

    return ApiResponse(200, "Mascotas obtenidas.", data).to_response()


# ─────────────────────────────────────
# OBTENER MASCOTA POR ID
# ─────────────────────────────────────
@mascota_bp.route("/<int:id_mascota>", methods=["GET"])
@jwt_required
def obtener_mascota(id_mascota):

    """
    Obtener mascota por ID
    ---
    tags:
      - Mascotas
    description: Obtener información de una mascota específica.

    parameters:

      - name: Authorization
        in: header
        type: string
        required: true

      - name: id_mascota
        in: path
        type: integer
        required: true
        example: 1

    responses:
      200:
        description: Mascota encontrada
      404:
        description: Mascota no encontrada
    """

    try:

        m = mascota_service.obtener_mascota(id_mascota)

        data = {
            "id_mascota": m.id_mascota,
            "id_usuario": m.id_usuario,
            "nombre": m.nombre,
            "especie": m.especie,
            "raza": m.raza,
            "color": m.color,
            "sexo": m.sexo,
            "edad": m.edad,
            "estado": m.estado,
            "descripcion": m.descripcion,
            "foto_url": m.foto_url,
            "direccion_hogar": m.direccion_hogar,
            "latitud_hogar": float(m.latitud_hogar) if m.latitud_hogar else None,
            "longitud_hogar": float(m.longitud_hogar) if m.longitud_hogar else None,
            "fecha_registro": m.fecha_registro
        }

        return ApiResponse(200, "Mascota encontrada.", data).to_response()

    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()


# ─────────────────────────────────────
# ACTUALIZAR MASCOTA
# ─────────────────────────────────────
@mascota_bp.route("/<int:id_mascota>", methods=["PUT"])
@jwt_required
def actualizar_mascota(id_mascota):

    """
    Actualizar mascota
    ---
    tags:
      - Mascotas
    description: Actualiza datos de una mascota.

    consumes:
      - multipart/form-data

    parameters:

      - name: Authorization
        in: header
        type: string
        required: true

      - name: id_mascota
        in: path
        type: integer
        required: true

      - name: nombre
        in: formData
        type: string

      - name: especie
        in: formData
        type: string

      - name: raza
        in: formData
        type: string

      - name: estado
        in: formData
        type: string
        example: PERDIDA

      - name: foto
        in: formData
        type: file

    responses:
      200:
        description: Mascota actualizada
      404:
        description: Mascota no encontrada
    """

    try:

        data = UpdateMascotaDto.from_request(request.form.to_dict())
        file = request.files.get("foto")

        mascota = mascota_service.actualizar_mascota(id_mascota, data, file)

        return ApiResponse(200, "Mascota actualizada.", {
            "id_mascota": mascota.id_mascota
        }).to_response()

    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()


# ─────────────────────────────────────
# ELIMINAR MASCOTA
# ─────────────────────────────────────
@mascota_bp.route("/<int:id_mascota>", methods=["DELETE"])
@jwt_required
def eliminar_mascota(id_mascota):

    """
    Eliminar mascota
    ---
    tags:
      - Mascotas
    description: Elimina una mascota y su imagen.

    parameters:

      - name: Authorization
        in: header
        type: string
        required: true

      - name: id_mascota
        in: path
        type: integer
        required: true

    responses:
      200:
        description: Mascota eliminada
      404:
        description: Mascota no encontrada
    """

    try:

        mascota_service.eliminar_mascota(id_mascota)

        return ApiResponse(200, "Mascota eliminada correctamente.", True).to_response()

    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()