from flask import Blueprint, request, g
from app.UsuarioModule.Service.UsuarioService import UsuarioService
from app.UsuarioModule.Dto.CrearUsuarioDTO import CrearUsuarioDTO
from app.UsuarioModule.Dto.ActualizarUsuarioDTO import ActualizarUsuarioDTO
from app.config.Dto.ApiResponse import ApiResponse
from app.config.JwtFilter import jwt_required

usuario_bp = Blueprint("usuarios", __name__)
service = UsuarioService()


@usuario_bp.route("/me", methods=["GET"])
@jwt_required
def get_me():
    usuario = service.get_by_id(g.user_id)
    return ApiResponse(200, "OK", usuario.to_dict()).to_response()


@usuario_bp.route("/registro", methods=["POST"])
def register():
    try:
        data = request.get_json()

        dto = CrearUsuarioDTO(
            nombre=data.get("nombre"),
            correo=data.get("correo"),
            password=data.get("password"),
            telefono=data.get("telefono")
        )

        usuario = service.crear(dto)
        return ApiResponse(201, "Usuario creado", usuario.to_dict()).to_response()

    except ValueError as e:
        return ApiResponse(409, str(e)).to_response()


@usuario_bp.route("/me", methods=["PUT"])
@jwt_required
def actualizar_me():
    data = request.get_json()

    dto = ActualizarUsuarioDTO(
        nombre=data.get("nombre"),
        correo=data.get("correo"),
        password=data.get("password"),
        telefono=data.get("telefono"),
        activo=data.get("activo")
    )

    usuario = service.actualizar(g.user_id, dto)
    return ApiResponse(200, "Actualizado", usuario.to_dict()).to_response()


@usuario_bp.route("/me", methods=["DELETE"])
@jwt_required
def eliminar_me():
    service.eliminar(g.user_id)
    return ApiResponse(200, "Usuario eliminado").to_response()