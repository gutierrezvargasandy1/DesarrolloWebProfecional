# app/services/email_service.py

import os
import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText


class EmailService:

    def __init__(self):
        self.host     = os.getenv("MAIL_HOST", "smtp.gmail.com")
        self.port     = int(os.getenv("MAIL_PORT", 587))
        self.usuario  = os.getenv("MAIL_USER")
        self.password = os.getenv("MAIL_PASSWORD")
        self.remitente = os.getenv("MAIL_FROM", self.usuario)

    def enviar_codigo_recuperacion(self, destinatario: str, nombre: str, codigo: str):
        asunto = "Recuperación de contraseña"
        html   = self._template_recuperacion(nombre, codigo)
        self._enviar(destinatario, asunto, html)

    def _enviar(self, destinatario: str, asunto: str, html: str):
        msg = MIMEMultipart("alternative")
        msg["Subject"] = asunto
        msg["From"]    = self.remitente
        msg["To"]      = destinatario
        msg.attach(MIMEText(html, "html"))

        with smtplib.SMTP(self.host, self.port) as server:
            server.ehlo()
            server.starttls()
            server.login(self.usuario, self.password)
            server.sendmail(self.remitente, destinatario, msg.as_string())

    def _template_recuperacion(self, nombre: str, codigo: str) -> str:
        return f"""
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8"/>
            <style>
                body      {{ font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }}
                .container{{ max-width: 500px; margin: 40px auto; background: #fff; border-radius: 10px;
                             padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }}
                .header   {{ text-align: center; padding-bottom: 20px; border-bottom: 1px solid #eee; }}
                .header h1{{ color: #333; font-size: 22px; }}
                .codigo   {{ text-align: center; margin: 30px 0; }}
                .codigo span{{ display: inline-block; font-size: 36px; font-weight: bold;
                              letter-spacing: 10px; color: #4F46E5; background: #EEF2FF;
                              padding: 15px 30px; border-radius: 8px; }}
                .footer   {{ text-align: center; color: #999; font-size: 12px; margin-top: 20px; }}
                p         {{ color: #555; line-height: 1.6; }}
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>🐾 Recuperación de contraseña</h1>
                </div>
                <p>Hola <strong>{nombre}</strong>,</p>
                <p>Recibimos una solicitud para restablecer tu contraseña. Usa el siguiente código:</p>
                <div class="codigo">
                    <span>{codigo}</span>
                </div>
                <p>Este código es válido por <strong>5 minutos</strong>. Si no solicitaste este cambio, ignora este correo.</p>
                <div class="footer">
                    <p>© 2025 PetFinder · No respondas este correo</p>
                </div>
            </div>
        </body>
        </html>
        """