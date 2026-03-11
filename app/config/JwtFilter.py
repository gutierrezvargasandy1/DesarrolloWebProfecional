import os
import jwt
from functools import wraps
from flask import request, jsonify
from datetime import datetime, timedelta

SECRET_KEY = os.getenv("SECRET_KEY")

# ── Generar token ──────────────────────────────────────────
def generate_token(user_id: int, role: str = "user") -> str:
    payload = {
        "sub": user_id,
        "role": role,
        "iat": datetime.utcnow(),
        "exp": datetime.utcnow() + timedelta(hours=int(os.getenv("JWT_EXPIRATION_HOURS", 24)))
    }
    return jwt.encode(payload, SECRET_KEY, algorithm="HS256")


# ── Decorator (equivalente a @Secured / @PreAuthorize) ─────
def jwt_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None

        if "Authorization" in request.headers:
            auth = request.headers["Authorization"]
            if auth.startswith("Bearer "):
                token = auth.split(" ")[1]

        if not token:
            return jsonify({"status": 401, "message": "Token requerido"}), 401

        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
            request.current_user = payload
        except jwt.ExpiredSignatureError:
            return jsonify({"status": 401, "message": "Token expirado"}), 401
        except jwt.InvalidTokenError:
            return jsonify({"status": 401, "message": "Token inválido"}), 401

        return f(*args, **kwargs)
    return decorated


# ── Decorator por rol (equivalente a @PreAuthorize("hasRole('ADMIN')")) ──
def role_required(*roles):
    def decorator(f):
        @wraps(f)
        @jwt_required
        def decorated(*args, **kwargs):
            if request.current_user.get("role") not in roles:
                return jsonify({"status": 403, "message": "No tienes permisos"}), 403
            return f(*args, **kwargs)
        return decorated
    return decorator