import os
from dotenv import load_dotenv
from flask_sqlalchemy import SQLAlchemy

load_dotenv()

db = SQLAlchemy()

class DataBaseConfig:
    SQLALCHEMY_DATABASE_URI = (
        f"postgresql+psycopg2://"
        f"{os.getenv('DB_USER')}:{os.getenv('DB_PASSWORD')}"
        f"@{os.getenv('DB_HOST')}:{os.getenv('DB_PORT')}"
        f"/{os.getenv('DB_NAME')}"
    )
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    SECRET_KEY = os.getenv("SECRET_KEY")

    @staticmethod
    def init_app(app):
        app.config["SQLALCHEMY_DATABASE_URI"] = DataBaseConfig.SQLALCHEMY_DATABASE_URI
        app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = DataBaseConfig.SQLALCHEMY_TRACK_MODIFICATIONS
        app.config["SECRET_KEY"] = DataBaseConfig.SECRET_KEY
        db.init_app(app)