package mx.edu.utng.petfinder.data.remote.UsuariosModule.Dto

data class UsuarioDto(
    val id_usuario: Int?,
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val activo: Boolean?
)

data class CrearUsuarioRequest(
    val nombre: String,
    val correo: String,
    val password: String,
    val telefono: String?
)

data class ActualizarUsuarioRequest(
    val nombre: String?,
    val correo: String?,
    val password: String?,
    val telefono: String?,
    val activo: Boolean?
)
