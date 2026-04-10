import os
import jwt
from functools import wraps
from flask import request, jsonify, g
from datetime import datetime, timedelta

SECRET_KEY = os.getenv("SECRET_KEY")


# ── Generar token ──────────────────────────────────────────
def generate_token(user_id: int, role: str = "user") -> str:
    payload = {
        "sub": user_id,          # id del usuario
        "role": role,           # rol del usuario
        "iat": datetime.utcnow(),
        "exp": datetime.utcnow() + timedelta(
            hours=int(os.getenv("JWT_EXPIRATION_HOURS", 24))
        ),
    }
    return jwt.encode(payload, SECRET_KEY, algorithm="HS256")


# ── Decorator JWT (Interceptor real) ───────────────────────
def jwt_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth_header = request.headers.get("Authorization")

        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"status": 401, "message": "Token requerido"}), 401

        token = auth_header.split(" ")[1]

        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])

            g.user_id = payload["sub"]
            g.role    = payload["role"]

        except jwt.ExpiredSignatureError:
            return jsonify({"status": 401, "message": "Token expirado"}), 401
        except jwt.InvalidTokenError:
            return jsonify({"status": 401, "message": "Token inválido"}), 401

        return f(*args, **kwargs)

    return decorated


# ── Decorator por rol ──────────────────────────────────────
def role_required(*roles):
    def decorator(f):
        @wraps(f)
        @jwt_required
        def decorated(*args, **kwargs):
            if g.role not in roles:
                return jsonify({"status": 403, "message": "No tienes permisos"}), 403
            return f(*args, **kwargs)
        return decorated
    return decorator