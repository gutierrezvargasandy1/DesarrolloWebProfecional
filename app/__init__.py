from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from app.config.config import config
from flasgger import Swagger

db = SQLAlchemy()
migrate = Migrate()


def create_app(config_name="default"):
    app = Flask(__name__)
    app.config.from_object(config[config_name])

    # Inicializar base de datos
    db.init_app(app)
    migrate.init_app(app, db)

    # ─────────────────────────────────────
    # IMPORTAR MODELOS
    # ─────────────────────────────────────
    from app.UsuarioModule.Model.Usuario import Usuario
    from app.MascotaModule.Model.Mascota import Mascota
    from app.ReporteModule.Model.ReporteMascota import ReporteMascota
    from app.AvistamientoModule.Model.Avistamiento import Avistamiento
    from app.ConversacionModule.Model.Conversacion import Conversacion
    from app.ConversacionModule.Model.Mensaje import Mensaje


    # ─────────────────────────────────────
    # IMPORTAR CONTROLADORES
    # ─────────────────────────────────────
    from app.LoginModule.controller.AuthController import auth_bp
    from app.UsuarioModule.controller.UsuarioController import usuario_bp
    from app.MascotaModule.controller.MascotaContoller import mascota_bp
    from app.utils.file_controller import file_bp
    from app.ReporteModule.controller.ReporteMascotaController import reporte_bp

    # ─────────────────────────────────────
    # REGISTRAR RUTAS
    # ─────────────────────────────────────
    app.register_blueprint(auth_bp, url_prefix="/api/auth")
    app.register_blueprint(usuario_bp, url_prefix="/api/usuarios")
    app.register_blueprint(mascota_bp, url_prefix="/api/mascotas")
    app.register_blueprint(file_bp, url_prefix="/files")
    app.register_blueprint(reporte_bp, url_prefix="/api/reportes")

    # ─────────────────────────────────────
    # CONFIGURACIÓN SWAGGER
    # ─────────────────────────────────────
    swagger_template = {
        "swagger": "2.0",
        "info": {
            "title": "API Sistema Mascotas Perdidas",
            "description": "API para gestión de usuarios, mascotas, reportes y avistamientos.",
            "version": "1.0.0"
        },

        "securityDefinitions": {
            "BearerAuth": {
                "type": "apiKey",
                "name": "Authorization",
                "in": "header",
                "description": "JWT Authorization header usando esquema: Bearer {token}"
            }
        },

        "security": [
            {
                "BearerAuth": []
            }
        ]
    }


    swagger_config = {
        "headers": [],
        "specs": [
            {
                "endpoint": "apispec",
                "route": "/apispec.json",
                "rule_filter": lambda rule: True,
                "model_filter": lambda tag: True,
            }
        ],

        "swagger_ui": True,
        "specs_route": "/swagger/",

        # CDN para evitar errores locales
        "swagger_ui_bundle_js": "https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-bundle.js",
        "swagger_ui_standalone_preset_js": "https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-standalone-preset.js",
        "swagger_ui_css": "https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui.css",

        "swagger_ui_favicon_32": "https://unpkg.com/swagger-ui-dist@5.11.0/favicon-32x32.png",
        "swagger_ui_favicon_16": "https://unpkg.com/swagger-ui-dist@5.11.0/favicon-16x16.png",
    }


    Swagger(app, template=swagger_template, config=swagger_config)

    return app