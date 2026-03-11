Flask MVC API 🐾
Estructura backend en Flask siguiendo el patrón MVC.
Estructura del proyecto
flask_mvc/
├── app/
│   ├── __init__.py                      # App factory + registro de blueprints
│   ├── controllers/
│   │   ├── user_controller.py           # CRUD de usuarios
│   │   └── auth_controller.py           # Login y recuperación de contraseña
│   ├── models/
│   │   └── models.py                    # Todos los modelos (Usuario, Mascota, Reporte, etc.)
│   ├── views/
│   │   └── user_view.py                 # ApiResponse genérico
│   ├── services/
│   │   ├── usuario_service.py           # Lógica de negocio de usuarios
│   │   ├── auth_service.py              # Login + recuperación de contraseña
│   │   └── email_service.py             # Envío de correos HTML
│   ├── static/
│   └── templates/
├── config/
│   ├── config.py                        # Configuración por entorno + PostgreSQL
│   └── jwt_config.py                    # JWT: generate_token, jwt_required, role_required
├── tests/
│   └── test_users.py
├── .env                                 # Variables de entorno (no subir a git)
├── .env.example                         # Plantilla de variables de entorno
├── requirements.txt
└── run.py                               # Punto de entrada
Instalación
bash# 1. Crear entorno virtual
python -m venv venv
source venv/bin/activate        # Linux/Mac
venv\Scripts\activate           # Windows

# 2. Instalar dependencias
pip install -r requirements.txt

# 3. Configurar variables de entorno
cp .env.example .env
# Edita .env con tus valores

# 4. Inicializar base de datos
flask --app run db init
flask --app run db migrate -m "Inicial"
flask --app run db upgrade

# 5. Ejecutar la aplicación
python run.py
Variables de entorno (.env)
env# App
FLASK_ENV=development
FLASK_HOST=0.0.0.0
FLASK_PORT=5000
SECRET_KEY=tu_clave_secreta

# PostgreSQL
DB_USER=postgres
DB_PASSWORD=tu_contraseña
DB_HOST=localhost
DB_PORT=5432
DB_NAME=flask_db

# JWT
JWT_EXPIRATION_HOURS=24

# Correo (Gmail)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=tu_correo@gmail.com
MAIL_PASSWORD=tu_app_password
MAIL_FROM=tu_correo@gmail.com
Endpoints disponibles
👤 Usuarios — /api/usuarios
MétodoRutaDescripciónProtegidoGET/api/usuarios/Obtener todos jwtGET/api/usuarios/<id>Obtener por ID jwtPOST/api/usuarios/registerRegistrar usuario públicoPUT/api/usuarios/<id>Actualizar usuario jwtDELETE/api/usuarios/<id>Eliminar usuario admin only
 Auth — /api/auth
MétodoRutaDescripciónProtegidoPOST/api/auth/loginLogin, retorna JWT públicoPOST/api/auth/recuperarEnvía código al correo públicoPOST/api/auth/verificar-codigoVerifica código (correo+código) públicoPOST/api/auth/cambiar-passwordCambia contraseña con código público
Modelos disponibles
ModeloTablaDescripciónUsuarioUsuariosUsuarios del sistemaMascotaMascotasMascotas registradasReporteMascotaReportesMascotaReportes de mascotas perdidasAvistamientoAvistamientosAvistamientos de mascotasConversacionConversacionesConversaciones entre usuariosMensajeMensajesMensajes dentro de conversaciones
Seguridad JWT
Decoradores disponibles en config/jwt_config.py:
python# Requiere token válido
@jwt_required

# Requiere rol específico
@role_required("admin")

# Múltiples roles
@role_required("admin", "moderator")
El token se obtiene del header:
Authorization: Bearer <token>
Ejecutar tests
bashpytest tests/










pip install psycopg2-binary --only-binary :all: