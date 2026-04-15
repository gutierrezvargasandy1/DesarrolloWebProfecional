import os
import jwt
from functools import wraps
from flask import request, jsonify, g
from datetime import datetime, timedelta

SECRET_KEY = os.getenv("SECRET_KEY")

ACCESS_EXP_MIN = int(os.getenv("JWT_ACCESS_MIN", 15))
REFRESH_EXP_DAYS = int(os.getenv("JWT_REFRESH_DAYS", 7))


def generate_access_token(user_id: int, role: str) -> str:
    payload = {
        "sub": user_id,
        "role": role,
        "type": "access",
        "iat": datetime.utcnow(),
        "exp": datetime.utcnow() + timedelta(minutes=ACCESS_EXP_MIN),
    }
    return jwt.encode(payload, SECRET_KEY, algorithm="HS256")


def generate_refresh_token(user_id: int) -> str:
    payload = {
        "sub": user_id,
        "type": "refresh",
        "iat": datetime.utcnow(),
        "exp": datetime.utcnow() + timedelta(days=REFRESH_EXP_DAYS),
    }
    return jwt.encode(payload, SECRET_KEY, algorithm="HS256")


def decode_token(token: str):
    return jwt.decode(token, SECRET_KEY, algorithms=["HS256"])


# ── JWT REQUIRED (access token) ────────────────────────────
def jwt_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth_header = request.headers.get("Authorization")

        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"status": 401, "message": "Token requerido"}), 401

        token = auth_header.split(" ")[1]

        try:
            payload = decode_token(token)

            if payload.get("type") != "access":
                raise jwt.InvalidTokenError()

            g.user_id = payload["sub"]
            g.role = payload["role"]

        except jwt.ExpiredSignatureError:
            return jsonify({"status": 401, "message": "Token expirado"}), 401
        except jwt.InvalidTokenError:
            return jsonify({"status": 401, "message": "Token inválido"}), 401

        return f(*args, **kwargs)

    return decorated