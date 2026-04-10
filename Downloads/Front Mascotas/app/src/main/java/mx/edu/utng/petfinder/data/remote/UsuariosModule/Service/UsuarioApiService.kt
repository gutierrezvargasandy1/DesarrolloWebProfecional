package mx.edu.utng.petfinder.data.remote.UsuariosModule.Service
import mx.edu.utng.petfinder.data.remote.ConfigClient.ApiResponse
import mx.edu.utng.petfinder.data.remote.UsuariosModule.Dto.ActualizarUsuarioRequest
import mx.edu.utng.petfinder.data.remote.UsuariosModule.Dto.CrearUsuarioRequest
import mx.edu.utng.petfinder.data.remote.UsuariosModule.Dto.UsuarioDto
import retrofit2.http.*

interface UsuarioApiService {

    // ───────── ME ─────────
    @GET("api/usuarios/me")
    suspend fun getMe(): ApiResponse<UsuarioDto>

    // ───────── REGISTER ─────────
    @POST("api/usuarios/registro")
    suspend fun register(
        @Body request: CrearUsuarioRequest
    ): ApiResponse<UsuarioDto>

    // ───────── UPDATE ME ─────────
    @PUT("api/usuarios/me")
    suspend fun updateMe(
        @Body request: ActualizarUsuarioRequest
    ): ApiResponse<UsuarioDto>

    // ───────── DELETE ME ─────────
    @DELETE("api/usuarios/me")
    suspend fun deleteMe(): ApiResponse<String>
}