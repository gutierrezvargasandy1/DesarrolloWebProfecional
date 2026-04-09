from flask import Blueprint, request, jsonify
from app.ConversacionModule.Service.ConversacionService import ConversacionService

conversacion_bp = Blueprint("conversaciones", __name__, url_prefix="/api/conversaciones")
service = ConversacionService()


# ── GET /api/conversaciones/usuario/<id_usuario> ───────────
@conversacion_bp.route("/usuario/<int:id_usuario>", methods=["GET"])
def get_conversaciones_usuario(id_usuario):
    try:
        return jsonify(service.obtener_conversaciones_usuario(id_usuario)), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── GET /api/conversaciones/<id_conversacion> ──────────────
@conversacion_bp.route("/<int:id_conversacion>", methods=["GET"])
def get_conversacion(id_conversacion):
    try:
        resultado = service.obtener_conversacion_por_id(id_conversacion)
        if not resultado:
            return jsonify({"error": "Conversación no encontrada"}), 404
        return jsonify(resultado), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── POST /api/conversaciones/ ──────────────────────────────
# Body: { id_reporte, id_usuario_1, id_usuario_2 }
@conversacion_bp.route("/", methods=["POST"])
def crear_conversacion():
    try:
        data = request.get_json()
        id_reporte   = data.get("id_reporte")
        id_usuario_1 = data.get("id_usuario_1")
        id_usuario_2 = data.get("id_usuario_2")

        if not all([id_reporte, id_usuario_1, id_usuario_2]):
            return jsonify({"error": "id_reporte, id_usuario_1 y id_usuario_2 son requeridos"}), 400

        resultado = service.crear_conversacion(id_reporte, id_usuario_1, id_usuario_2)
        return jsonify(resultado), 201
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── POST /api/conversaciones/<id_conversacion>/mensajes ────
# Body: { id_emisor, mensaje, tipo }
@conversacion_bp.route("/<int:id_conversacion>/mensajes", methods=["POST"])
def enviar_mensaje(id_conversacion):
    try:
        data      = request.get_json()
        id_emisor = data.get("id_emisor")
        mensaje   = data.get("mensaje")
        tipo      = data.get("tipo", "texto")

        if not all([id_emisor, mensaje]):
            return jsonify({"error": "id_emisor y mensaje son requeridos"}), 400

        resultado = service.enviar_mensaje(id_conversacion, id_emisor, mensaje, tipo)
        return jsonify(resultado), 201
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except PermissionError as e:
        return jsonify({"error": str(e)}), 403
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── GET /api/conversaciones/<id_conversacion>/mensajes ─────
@conversacion_bp.route("/<int:id_conversacion>/mensajes", methods=["GET"])
def get_mensajes(id_conversacion):
    try:
        return jsonify(service.obtener_mensajes(id_conversacion)), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500