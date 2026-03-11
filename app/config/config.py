import os
from dotenv import load_dotenv

load_dotenv()


class Config:
    """Configuración base."""
    SECRET_KEY = os.getenv("SECRET_KEY", "clave-secreta-por-defecto")
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    DEBUG = False
    TESTING = False


class DevelopmentConfig(Config):
    """Configuración de desarrollo (PostgreSQL)."""
    DEBUG = True
    SQLALCHEMY_DATABASE_URI = (
        f"postgresql+psycopg2://"
        f"{os.getenv('DB_USER')}:{os.getenv('DB_PASSWORD')}"
        f"@{os.getenv('DB_HOST')}:{os.getenv('DB_PORT')}"
        f"/{os.getenv('DB_NAME')}"
    )


class TestingConfig(Config):
    """Configuración de pruebas (SQLite opcional)."""
    TESTING = True
    SQLALCHEMY_DATABASE_URI = os.getenv(
        "TEST_DATABASE_URL", "sqlite:///test.db"
    )


class ProductionConfig(Config):
    """Configuración de producción (PostgreSQL)."""
    SQLALCHEMY_DATABASE_URI = (
        f"postgresql+psycopg2://"
        f"{os.getenv('DB_USER')}:{os.getenv('DB_PASSWORD')}"
        f"@{os.getenv('DB_HOST')}:{os.getenv('DB_PORT')}"
        f"/{os.getenv('DB_NAME')}"
    )


config = {
    "development": DevelopmentConfig,
    "testing": TestingConfig,
    "production": ProductionConfig,
    "default": DevelopmentConfig,
}