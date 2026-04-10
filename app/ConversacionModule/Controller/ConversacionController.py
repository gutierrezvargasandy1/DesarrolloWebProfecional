from flask import Blueprint, request, jsonify, g
from app.config.JwtFilter import jwt_required
from app.ConversacionModule.Service.ConversacionService import ConversacionService

conversacion_bp = Blueprint("conversaciones", __name__, url_prefix="/api/conversaciones")
service = ConversacionService()


# ── GET /api/conversaciones/mias ───────────────────────────
@conversacion_bp.route("/mias", methods=["GET"])
@jwt_required
def get_conversaciones_usuario():
    try:
        return jsonify(service.obtener_conversaciones_usuario(g.user_id)), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── GET /api/conversaciones/<id_conversacion> ──────────────
@conversacion_bp.route("/<int:id_conversacion>", methods=["GET"])
@jwt_required
def get_conversacion(id_conversacion):
    try:
        resultado = service.obtener_conversacion_por_id(id_conversacion, g.user_id)
        return jsonify(resultado), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except PermissionError as e:
        return jsonify({"error": str(e)}), 403
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── POST /api/conversaciones/ ──────────────────────────────
# Body: { id_reporte, id_usuario_destino }
@conversacion_bp.route("/", methods=["POST"])
@jwt_required
def crear_conversacion():
    try:
        data = request.get_json()

        id_reporte = data.get("id_reporte")
        id_usuario_destino = data.get("id_usuario_destino")

        if not all([id_reporte, id_usuario_destino]):
            return jsonify({"error": "id_reporte e id_usuario_destino son requeridos"}), 400

        resultado = service.crear_conversacion(
            id_reporte,
            g.user_id,          # ← usuario autenticado
            id_usuario_destino
        )

        return jsonify(resultado), 201

    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── POST /api/conversaciones/<id_conversacion>/mensajes ────
# Body: { mensaje, tipo }
@conversacion_bp.route("/<int:id_conversacion>/mensajes", methods=["POST"])
@jwt_required
def enviar_mensaje(id_conversacion):
    try:
        data = request.get_json()

        mensaje = data.get("mensaje")
        tipo = data.get("tipo", "texto")

        if not mensaje:
            return jsonify({"error": "mensaje es requerido"}), 400

        resultado = service.enviar_mensaje(
            id_conversacion,
            g.user_id,     # ← emisor autenticado
            mensaje,
            tipo
        )

        return jsonify(resultado), 201

    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except PermissionError as e:
        return jsonify({"error": str(e)}), 403
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── GET /api/conversaciones/<id_conversacion>/mensajes ─────
@conversacion_bp.route("/<int:id_conversacion>/mensajes", methods=["GET"])
@jwt_required
def get_mensajes(id_conversacion):
    try:
        return jsonify(service.obtener_mensajes(id_conversacion, g.user_id)), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except PermissionError as e:
        return jsonify({"error": str(e)}), 403
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ── DELETE /api/conversaciones/<id_conversacion> ───────────
@conversacion_bp.route("/<int:id_conversacion>", methods=["DELETE"])
@jwt_required
def borrar_conversacion(id_conversacion):
    try:
        resultado = service.borrar_conversacion(id_conversacion, g.user_id)
        return jsonify(resultado), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except PermissionError as e:
        return jsonify({"error": str(e)}), 403
    except Exception as e:
        return jsonify({"error": str(e)}), 500