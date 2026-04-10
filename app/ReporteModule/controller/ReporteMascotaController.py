from flask import Blueprint, request, g
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
    reportes = service.get_all_by_user(g.user_id)
    return ApiResponse(200, "OK", [r.id_reporte for r in reportes]).to_response()


@reporte_bp.route("/<int:id>", methods=["GET"])
@jwt_required
def get_by_id(id):
    try:
        reporte = service.get_by_id_and_user(id, g.user_id)
        return ApiResponse(200, "OK", {"id_reporte": reporte.id_reporte}).to_response()
    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()

@reporte_bp.route("/public", methods=["GET"])
def get_all_public():
    data = service.get_all_public_with_mascota()
    return ApiResponse(200, "OK", data).to_response()


@reporte_bp.route("/public/<int:id>", methods=["GET"])
def get_by_id_public(id):
    try:
        data = service.get_by_id_public_with_mascota(id)
        return ApiResponse(200, "OK", data).to_response()
    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()
    

@reporte_bp.route("/full", methods=["GET"])
@jwt_required
def get_all_full():
    data = service.get_all_with_mascota_by_user(g.user_id)
    return ApiResponse(200, "OK", data).to_response()


@reporte_bp.route("/<int:id>/full", methods=["GET"])
@jwt_required
def get_by_id_full(id):
    try:
        data = service.get_by_id_with_mascota_and_user(id, g.user_id)
        return ApiResponse(200, "OK", data).to_response()
    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()


@reporte_bp.route("/", methods=["POST"])
@jwt_required
def crear():
    try:
        data = request.get_json()

        dto = CrearReporteDTO(
            id_mascota=data.get("id_mascota"),
            descripcion=data.get("descripcion"),
            latitud=data.get("latitud"),
            longitud=data.get("longitud"),
            direccion=data.get("direccion"),
            estado=data.get("estado")
        )

        reporte = service.crear(dto, g.user_id)
        return ApiResponse(201, "Creado", {"id_reporte": reporte.id_reporte}).to_response()

    except ValueError as e:
        return ApiResponse(400, str(e)).to_response()


@reporte_bp.route("/<int:id>", methods=["PUT"])
@jwt_required
def actualizar(id):
    try:
        data = request.get_json()

        dto = ActualizarReporteDTO(
            descripcion=data.get("descripcion"),
            latitud=data.get("latitud"),
            longitud=data.get("longitud"),
            direccion=data.get("direccion"),
            estado=data.get("estado")
        )

        reporte = service.actualizar(id, dto, g.user_id)
        return ApiResponse(200, "Actualizado", {"id_reporte": reporte.id_reporte}).to_response()

    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()


@reporte_bp.route("/<int:id>", methods=["DELETE"])
@jwt_required
def eliminar(id):
    try:
        service.eliminar(id, g.user_id)
        return ApiResponse(200, "Eliminado").to_response()
    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()
    
@reporte_bp.route("/<int:id>/avistamientos", methods=["POST"])
@jwt_required
def crear_avistamiento(id):
    try:
        data = request.get_json()
        resultado = service.crear_avistamiento(id, g.user_id, data)
        return ApiResponse(201, "Avistamiento creado", resultado).to_response()
    except ValueError as e:
        return ApiResponse(400, str(e)).to_response()

@reporte_bp.route("/avistamientos/<int:id_avistamiento>", methods=["PUT"])
@jwt_required
def editar_avistamiento(id_avistamiento):
    user_id = request.current_user.get("sub")
    data = request.get_json()

    actualizado = service.actualizar_avistamiento(id_avistamiento, user_id, data)
    return ApiResponse(200, "Avistamiento actualizado", actualizado).to_response()