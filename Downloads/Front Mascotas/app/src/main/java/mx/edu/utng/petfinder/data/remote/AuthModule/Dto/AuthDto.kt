package mx.edu.utng.petfinder.data.remote.AuthModule.Dto

object  AuthDto {

    // ───────────────────────── LOGIN ─────────────────────────

    data class LoginRequest(
        val correo: String,
        val password: String
    )

    data class LoginResponse(
        val token: String
    )

    // ─────────────── SOLICITAR RECUPERACIÓN ────────────────

    data class RecuperarPasswordRequest(
        val correo: String
    )


    // ─────────────── VERIFICAR CÓDIGO ───────────────────────

    data class VerificarCodigoRequest(
        val correo: String,
        val codigo: String
    )


    // ─────────────── CAMBIAR PASSWORD ───────────────────────

    data class CambiarPasswordRequest(
        val correo: String,
        val codigo: String,
        val nueva_password: String
    )

}