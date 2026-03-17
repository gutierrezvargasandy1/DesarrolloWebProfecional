from flask import Blueprint, request
from app.UsuarioModule.Service.UsuarioService import UsuarioService
from app.UsuarioModule.Dto.CrearUsuarioDTO import CrearUsuarioDTO
from app.UsuarioModule.Dto.ActualizarUsuarioDTO import ActualizarUsuarioDTO
from app.config.Dto.ApiResponse import ApiResponse
from app.config.JwtFilter import jwt_required, role_required

usuario_bp = Blueprint("usuarios", __name__)
service    = UsuarioService()


@usuario_bp.route("/", methods=["GET"])
@jwt_required
def get_all():
    """
    Obtener todos los usuarios
    ---
    tags:
      - Usuarios
    description: Devuelve la lista de usuarios activos registrados en el sistema.
    security:
      - BearerAuth: []
    responses:
      200:
        description: Lista de usuarios
        schema:
          type: array
          items:
            type: object
      401:
        description: Token JWT inválido o faltante
    """
    usuarios = service.get_all()
    return ApiResponse(200, "OK", [u.to_dict() for u in usuarios]).to_response()


@usuario_bp.route("/<int:id>", methods=["GET"])
@jwt_required
def get_by_id(id):
    """
    Obtener usuario por ID
    ---
    tags:
      - Usuarios
    description: Obtiene la información de un usuario específico mediante su ID.
    security:
      - BearerAuth: []
    parameters:
      - name: id
        in: path
        type: integer
        required: true
        description: ID del usuario
    responses:
      200:
        description: Usuario encontrado
        schema:
          type: object
      404:
        description: Usuario no encontrado
      401:
        description: Token inválido
    """
    try:
        usuario = service.get_by_id(id)
        if not usuario:
            return ApiResponse(404, "Usuario no encontrado").to_response()
        return ApiResponse(200, "OK", usuario.to_dict()).to_response()
    except Exception as e:
        return ApiResponse(500, str(e)).to_response()


# ✅ REGISTRO CON DTO + SWAGGER
@usuario_bp.route("/registro", methods=["POST"])
def register():
    """
    Registrar usuario
    ---
    tags:
      - Usuarios
    description: Permite registrar un nuevo usuario en el sistema.
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            nombre:
              type: string
              example: Juan Pérez
            correo:
              type: string
              example: juan@email.com
            password:
              type: string
              example: 123456
            telefono:
              type: string
              example: 4611234567
    responses:
      201:
        description: Usuario creado correctamente
      409:
        description: El correo ya está registrado
    """
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


# ✅ ACTUALIZAR CON DTO + SWAGGER
@usuario_bp.route("/<int:id>", methods=["PUT"])
@jwt_required
def actualizar(id):
    """
    Actualizar usuario
    ---
    tags:
      - Usuarios
    description: Permite actualizar los datos de un usuario existente.
    security:
      - BearerAuth: []
    parameters:
      - name: id
        in: path
        type: integer
        required: true
        description: ID del usuario
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            nombre:
              type: string
              example: Juan Pérez
            correo:
              type: string
              example: nuevo@email.com
            telefono:
              type: string
              example: 4611234567
            password:
              type: string
              example: nuevaPassword
            activo:
              type: boolean
              example: true
    responses:
      200:
        description: Usuario actualizado correctamente
      404:
        description: Usuario no encontrado
      401:
        description: Token inválido
    """
    try:
        data = request.get_json()

        dto = ActualizarUsuarioDTO(
            nombre=data.get("nombre"),
            correo=data.get("correo"),
            password=data.get("password"),
            telefono=data.get("telefono"),
            activo=data.get("activo")
        )

        usuario = service.actualizar(id, dto)
        return ApiResponse(200, "Actualizado", usuario.to_dict()).to_response()

    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()


@usuario_bp.route("/<int:id>", methods=["DELETE"])
@role_required("admin")
def eliminar(id):
    """
    Eliminar usuario
    ---
    tags:
      - Usuarios
    description: Elimina (soft delete) un usuario del sistema. Solo accesible para administradores.
    security:
      - BearerAuth: []
    parameters:
      - name: id
        in: path
        type: integer
        required: true
        description: ID del usuario
    responses:
      200:
        description: Usuario eliminado correctamente
      404:
        description: Usuario no encontrado
      403:
        description: Acceso denegado (solo admin)
    """
    try:
        service.eliminar(id)
        return ApiResponse(200, "Usuario eliminado").to_response()
    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()