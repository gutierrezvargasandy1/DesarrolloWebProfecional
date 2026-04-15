# app/controllers/auth_controller.py
from flask import Blueprint, make_response, request
from app.LoginModule.Service.AuthService import AuthService
from app.utils.EmailService import EmailService
from app.UsuarioModule.Model.Usuario import Usuario
from app.config.JwtFilter import generate_access_token, generate_refresh_token, decode_token
from app.config.Dto.ApiResponse import ApiResponse

auth_bp      = Blueprint("auth", __name__)
auth_service = AuthService()
email_service = EmailService()


# ── LOGIN ──────────────────────────────────────────────────
@auth_bp.route("/login", methods=["POST"])
def login():
    try:
        data = request.get_json()
        usuario = auth_service.login(data["correo"], data["password"])

        access_token = generate_access_token(usuario.id_usuario, usuario.rol)
        refresh_token = generate_refresh_token(usuario.id_usuario)

        response = make_response(
            ApiResponse(200, "Login exitoso", {
                "accessToken": access_token
            }).to_response()
        )

        response.set_cookie(
            "refresh_token",
            refresh_token,
            httponly=True,
            secure=True,
            samesite="Strict",
            route= "auth/refresh",
            max_age=60 * 60 * 24 * 7
        )

        return response

    except ValueError as e:
        return ApiResponse(401, str(e)).to_response()


# ── REFRESH ────────────────────────────────────────────────
@auth_bp.route("/refresh", methods=["POST"])
def refresh():
    token = request.cookies.get("refresh_token")

    if not token:
        return ApiResponse(401, "No refresh token").to_response()

    try:
        payload = decode_token(token)

        if payload.get("type") != "refresh":
            raise Exception()

        new_access = generate_access_token(payload["sub"], "user")

        return ApiResponse(200, "Token renovado", {
            "accessToken": new_access
        }).to_response()

    except:
        return ApiResponse(401, "Refresh inválido").to_response()


# ── LOGOUT ─────────────────────────────────────────────────
@auth_bp.route("/logout", methods=["POST"])
def logout():
    response = make_response(
        ApiResponse(200, "Sesión cerrada", True).to_response()
    )
    response.delete_cookie("refresh_token")
    return response


# ── SOLICITAR RECUPERACIÓN ─────────────────────────────────
@auth_bp.route("/recuperar", methods=["POST"])
def solicitar_recuperacion():
 
    try:
        data    = request.get_json()
        correo  = data["correo"]
        usuario = Usuario.query.filter_by(correo=correo).first()
        codigo  = auth_service.solicitar_recuperacion(correo)

        email_service.enviar_codigo_recuperacion(
            destinatario = correo,
            nombre       = usuario.nombre,
            codigo       = codigo
        )

        return ApiResponse(200, "Código enviado al correo.", True).to_response()
    except ValueError as e:
        return ApiResponse(404, str(e)).to_response()


# ── VERIFICAR CÓDIGO ───────────────────────────────────────
@auth_bp.route("/verificar-codigo", methods=["POST"])
def verificar_codigo():

    try:
        data = request.get_json()
        valido = auth_service.verificar_codigo(data["correo"], data["codigo"])

        if not valido:
            return ApiResponse(400, "Código inválido.", False).to_response()

        return ApiResponse(200, "Código válido.", True).to_response()

    except ValueError as e:
        return ApiResponse(400, str(e)).to_response()


# ── CAMBIAR CONTRASEÑA ─────────────────────────────────────
@auth_bp.route("/cambiar-password", methods=["POST"])
def cambiar_password():
 
    try:
        data = request.get_json()
        auth_service.cambiar_password(
            correo           = data["correo"],
            codigo           = data["codigo"],
            nueva_password   = data["nueva_password"]
        )
        return ApiResponse(200, "Contraseña actualizada correctamente.", True).to_response()
    except ValueError as e:
        return ApiResponse(400, str(e)).to_response()