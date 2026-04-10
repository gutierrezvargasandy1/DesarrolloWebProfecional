from flask import Blueprint, request, g
from app.MascotaModule.Service.MascotaService import MascotaService
from app.MascotaModule.Dto.CreateMascotaDto import CreateMascotaDto
from app.MascotaModule.Dto.UpdateMascotaDto import UpdateMascotaDto
from app.config.Dto.ApiResponse import ApiResponse
from app.config.JwtFilter import jwt_required

mascota_bp = Blueprint("mascotas", __name__)
mascota_service = MascotaService()


@mascota_bp.route("/", methods=["POST"])
@jwt_required
def crear_mascota():
    data = CreateMascotaDto.from_request(request.form.to_dict())
    file = request.files.get("foto")

    mascota = mascota_service.crear_mascota(g.user_id, data, file)

    return ApiResponse(201, "Mascota creada correctamente.", {
        "id_mascota": mascota.id_mascota
    }).to_response()


@mascota_bp.route("/", methods=["GET"])
@jwt_required
def obtener_mascotas():

    mascotas = mascota_service.obtener_mascotas_por_usuario(g.user_id)

    data = [{
        "id_mascota": m.id_mascota,
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
    } for m in mascotas]

    return ApiResponse(200, "Mascotas obtenidas.", data).to_response()


@mascota_bp.route("/<int:id_mascota>", methods=["GET"])
@jwt_required
def obtener_mascota(id_mascota):

    m = mascota_service.obtener_mascota_usuario(id_mascota, g.user_id)

    data = {
        "id_mascota": m.id_mascota,
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


@mascota_bp.route("/<int:id_mascota>", methods=["PUT"])
@jwt_required
def actualizar_mascota(id_mascota):

    data = UpdateMascotaDto.from_request(request.form.to_dict())
    file = request.files.get("foto")

    mascota = mascota_service.actualizar_mascota_usuario(
        id_mascota,
        g.user_id,
        data,
        file
    )

    return ApiResponse(200, "Mascota actualizada.", {
        "id_mascota": mascota.id_mascota
    }).to_response()


@mascota_bp.route("/<int:id_mascota>", methods=["DELETE"])
@jwt_required
def eliminar_mascota(id_mascota):

    mascota_service.eliminar_mascota_usuario(id_mascota, g.user_id)

    return ApiResponse(200, "Mascota eliminada correctamente.", True).to_response()