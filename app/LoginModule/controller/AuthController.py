# app/controllers/auth_controller.py
from flask import Blueprint, request
from app.LoginModule.Service.AuthService import AuthService
from app.utils.EmailService import EmailService
from app.UsuarioModule.Model.Usuario import Usuario
from app.config.JwtFilter import generate_token
from app.config.Dto.ApiResponse import ApiResponse

auth_bp      = Blueprint("auth", __name__)
auth_service = AuthService()
email_service = EmailService()


# ── LOGIN ──────────────────────────────────────────────────
@auth_bp.route("/login", methods=["POST"])
def login():
    """
    Login de usuario
    ---
    tags:
      - Autenticación
    description: Permite a un usuario autenticarse y obtener un token JWT.
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            correo:
              type: string
              example: usuario@email.com
            password:
              type: string
              example: 123456
    responses:
      200:
        description: Login exitoso
        schema:
          type: object
          properties:
            token:
              type: string
            usuario:
              type: object
      401:
        description: Credenciales incorrectas
    """
    try:
        data    = request.get_json()
        usuario = auth_service.login(data["correo"], data["password"])
        token   = generate_token(user_id=usuario.id_usuario)
        return ApiResponse(200, "Login exitoso", {
            "token":   token
        }).to_response()
    except ValueError as e:
        return ApiResponse(401, str(e)).to_response()


# ── SOLICITAR RECUPERACIÓN ─────────────────────────────────
@auth_bp.route("/recuperar", methods=["POST"])
def solicitar_recuperacion():
    """
    Solicitar recuperación de contraseña
    ---
    tags:
      - Autenticación
    description: Envía un código de recuperación al correo del usuario.
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            correo:
              type: string
              example: usuario@email.com
    responses:
      200:
        description: Código enviado al correo
      404:
        description: Usuario no encontrado
    """
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
    """
    Verificar código de recuperación
    ---
    tags:
      - Autenticación
    description: Verifica si el código enviado al correo es válido.
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            correo:
              type: string
              example: usuario@email.com
            codigo:
              type: string
              example: 123456
    responses:
      200:
        description: Código válido
        schema:
          type: object
          properties:
            valido:
              type: boolean
      400:
        description: Código inválido
    """
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
    """
    Cambiar contraseña
    ---
    tags:
      - Autenticación
    description: Permite cambiar la contraseña del usuario utilizando el código de recuperación.
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            correo:
              type: string
              example: usuario@email.com
            codigo:
              type: string
              example: 123456
            nueva_password:
              type: string
              example: nuevaPassword123
    responses:
      200:
        description: Contraseña actualizada correctamente
      400:
        description: Error en la validación
    """
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