package mx.edu.utng.reposertedh.model

data class LoginRequest(
    val correo: String,
    val password: String
)

data class LoginResponse(
    val tokenJwt: String
)

data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T?
)

enum class TipoUsuarioEnum {
    CIUDADANO, AUTORIDAD, ADMIN
}

data class UsuarioRegistroRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val telefono: String,
    val password: String,
    val tipoUsuario: TipoUsuarioEnum
)

data class RegistroResponse(
    val registrado: Boolean
)

data class RecuperacionRequest(
    val correo: String
)

data class ValidacionCodigoRequest(
    val codigo: String,
    val correo: String
)

data class RecuperacionValidacionRequest(
    val correo: String,
    val nuevaPassword: String,
    val codigo: String
)