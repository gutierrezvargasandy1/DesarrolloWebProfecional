from flask import Blueprint, request
from app.ReporteModule.Service.ReporteMascotaService import ReporteMascotaService
from app.ReporteModule.Dto.CrearReporteDTO import CrearReporteDTO
from app.ReporteModule.Dto.ActualizarReporteDTO import ActualizarReporteDTO
from app.config.Dto.ApiResponse import ApiResponse
from app.config.JwtFilter import jwt_required

reporte_bp = Blueprint("reportes", __name__)
service = ReporteMascotaService()


@reporte_bp.route("/", methods=["GET"])
@jwt_required
def get_all():
    """
    Obtener todos los reportes
    ---
    tags:
      - Reportes
    description: Devuelve todos los reportes registrados.
    security:
      - BearerAuth: []
    responses:
      200:
        description: Lista de reportes
    """
    reportes = service.get_all()
    return ApiResponse(200, "OK", [r.id_reporte for r in reportes]).to_response()


@reporte_bp.route("/<int:id>", methods=["GET"])
@jwt_required
def get_by_id(id):
    """
    Obtener reporte por ID
    ---
    tags:
      - Reportes
    description: Obtiene un reporte específico.
    security:
      - BearerAuth: []
    parameters:
      - name: id
        in: path
        type: integer
        required: true
    responses:
      200:
        description: Reporte encontrado
      404:
        description: No encontrado
    """
    try:
        reporte = service.get_by_id(id)
        if not reporte:
            return ApiResponse(404, "Reporte no encontrado").to_response()

        return ApiResponse(200, "OK", {
            "id_reporte": reporte.id_reporte
        }).to_response()

    except Exception as e:
        return ApiResponse(500, str(e)).to_response()


# 🔥 TODOS CON MASCOTA
@reporte_bp.route("/full", methods=["GET"])
@jwt_required
def get_all_full():
    """
    Obtener todos los reportes con mascota
    ---
    tags:
      - Reportes
    description: Devuelve todos los reportes incluyendo la información de la mascota.
    security:
      - BearerAuth: []
    responses:
      200:
        description: Lista de reportes con mascota
    """
    data = service.get_all_with_mascota()
    return ApiResponse(200, "OK", data).to_response()


# 🔥 UNO CON MASCOTA
@reporte_bp.route("/<int:id>/full", methods=["GET"])
@jwt_required
def get_by_id_full(id):
    """
    Obtener reporte por ID con mascota
    ---
    tags:
      - Reportes
    description: Devuelve un reporte junto con los datos de la mascota.
    security:
      - BearerAuth: []
    parameters:
      - name: id
        in: path
        type: integer
        required: true
    responses:
      200:
        description: Reporte con mascota
      404:
        description: No encontrado
    """
    try:
        data = service.get_by_id_with_mascota(id)
        return ApiResponse(200, "OK", data).to_response()
    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()


# ✅ CREAR
@reporte_bp.route("/", methods=["POST"])
@jwt_required
def crear():
    """
    Crear reporte
    ---
    tags:
      - Reportes
    description: Crea un nuevo reporte de mascota.
    security:
      - BearerAuth: []
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            id_mascota:
              type: integer
              example: 1
            id_usuario:
              type: integer
              example: 2
            descripcion:
              type: string
              example: Se perdió cerca del parque
            latitud:
              type: number
              example: 20.52345
            longitud:
              type: number
              example: -100.81523
            direccion:
              type: string
              example: Parque central
            estado:
              type: string
              example: PERDIDA
    responses:
      201:
        description: Reporte creado
      400:
        description: Error de validación
    """
    try:
        data = request.get_json()

        dto = CrearReporteDTO(
            id_mascota=data.get("id_mascota"),
            id_usuario=data.get("id_usuario"),
            descripcion=data.get("descripcion"),
            latitud=data.get("latitud"),
            longitud=data.get("longitud"),
            direccion=data.get("direccion"),
            estado=data.get("estado")
        )

        reporte = service.crear(dto)
        return ApiResponse(201, "Creado", {"id_reporte": reporte.id_reporte}).to_response()

    except ValueError as e:
        return ApiResponse(400, str(e)).to_response()


# ✅ ACTUALIZAR
@reporte_bp.route("/<int:id>", methods=["PUT"])
@jwt_required
def actualizar(id):
    """
    Actualizar reporte
    ---
    tags:
      - Reportes
    description: Actualiza un reporte existente.
    security:
      - BearerAuth: []
    parameters:
      - name: id
        in: path
        type: integer
        required: true
      - name: body
        in: body
        schema:
          type: object
          properties:
            descripcion:
              type: string
            latitud:
              type: number
            longitud:
              type: number
            direccion:
              type: string
            estado:
              type: string
              example: ENCONTRADA
    responses:
      200:
        description: Actualizado correctamente
      404:
        description: No encontrado
    """
    try:
        data = request.get_json()

        dto = ActualizarReporteDTO(
            descripcion=data.get("descripcion"),
            latitud=data.get("latitud"),
            longitud=data.get("longitud"),
            direccion=data.get("direccion"),
            estado=data.get("estado")
        )

        reporte = service.actualizar(id, dto)
        return ApiResponse(200, "Actualizado", {"id_reporte": reporte.id_reporte}).to_response()

    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()


# ✅ ELIMINAR
@reporte_bp.route("/<int:id>", methods=["DELETE"])
@jwt_required
def eliminar(id):
    """
    Eliminar reporte
    ---
    tags:
      - Reportes
    description: Elimina un reporte del sistema.
    security:
      - BearerAuth: []
    parameters:
      - name: id
        in: path
        type: integer
        required: true
    responses:
      200:
        description: Eliminado correctamente
      404:
        description: No encontrado
    """
    try:
        service.eliminar(id)
        return ApiResponse(200, "Eliminado").to_response()
    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()