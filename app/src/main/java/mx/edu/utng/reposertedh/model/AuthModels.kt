package mx.edu.utng.reposertedh.model
data class LoginResponse(
    val token: String
)

data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T?
)

data class RegistroResponse(
    val id_usuario: Int,
    val nombre: String,
    val correo: String,
    val telefono: String
)

data class LoginRequest(
    val correo: String,
    val password: String
)

data class RecuperacionRequest(
    val correo: String
)

data class VerificarCodigoRequest(
    val correo: String,
    val codigo: String
)

data class CambiarPasswordRequest(
    val correo: String,
    val codigo: String,
    val nueva_password: String
)

data class UsuarioRegistroRequest(
    val nombre: String,
    val correo: String,
    val password: String,
    val telefono: String
)