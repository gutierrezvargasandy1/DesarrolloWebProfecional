from flask import Blueprint, request, jsonify
from app.AvistamientoModule.Service.AvistamientoService import AvistamientoService

avistamiento_bp = Blueprint("avistamientos", __name__, url_prefix="/api/avistamientos")
service = AvistamientoService()


# ── GET /api/avistamientos/ ────────────────────────────────
@avistamiento_bp.route("/", methods=["GET"])
def get_todos():
    try:
        return jsonify(service.obtener_todos()), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── GET /api/avistamientos/<id> ────────────────────────────
@avistamiento_bp.route("/<int:id_avistamiento>", methods=["GET"])
def get_por_id(id_avistamiento):
    try:
        resultado = service.obtener_por_id(id_avistamiento)
        if not resultado:
            return jsonify({"error": "Avistamiento no encontrado"}), 404
        return jsonify(resultado), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── GET /api/avistamientos/reporte/<id_reporte> ────────────
@avistamiento_bp.route("/reporte/<int:id_reporte>", methods=["GET"])
def get_por_reporte(id_reporte):
    try:
        return jsonify(service.obtener_por_reporte(id_reporte)), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── GET /api/avistamientos/usuario/<id_usuario> ────────────
@avistamiento_bp.route("/usuario/<int:id_usuario>", methods=["GET"])
def get_por_usuario(id_usuario):
    try:
        return jsonify(service.obtener_por_usuario(id_usuario)), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── POST /api/avistamientos/ ───────────────────────────────
# Body: { id_reporte, id_usuario, descripcion, latitud, longitud, foto_url }
@avistamiento_bp.route("/", methods=["POST"])
def crear():
    try:
        resultado = service.crear(request.get_json())
        return jsonify(resultado), 201
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── PUT /api/avistamientos/<id> ────────────────────────────
# Body: { descripcion, foto_url, latitud, longitud }
@avistamiento_bp.route("/<int:id_avistamiento>", methods=["PUT"])
def actualizar(id_avistamiento):
    try:
        resultado = service.actualizar(id_avistamiento, request.get_json())
        return jsonify(resultado), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── DELETE /api/avistamientos/<id> ────────────────────────
@avistamiento_bp.route("/<int:id_avistamiento>", methods=["DELETE"])
def eliminar(id_avistamiento):
    try:
        resultado = service.eliminar(id_avistamiento)
        return jsonify(resultado), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500