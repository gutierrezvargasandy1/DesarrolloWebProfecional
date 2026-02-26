package mx.edu.utng.reposertedh.network

import mx.edu.utng.reposertedh.model.ApiResponse
import mx.edu.utng.reposertedh.model.LoginRequest
import mx.edu.utng.reposertedh.model.LoginResponse
import mx.edu.utng.reposertedh.model.RecuperacionRequest
import mx.edu.utng.reposertedh.model.RecuperacionValidacionRequest
import mx.edu.utng.reposertedh.model.RegistroResponse
import mx.edu.utng.reposertedh.model.UsuarioRegistroRequest
import mx.edu.utng.reposertedh.model.ValidacionCodigoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("api/usuarios/registro")
    suspend fun registro(@Body request: UsuarioRegistroRequest): Response<ApiResponse<RegistroResponse>>
    @POST("api/auth/recuperacion/enviar")
    suspend fun enviarCodigo(@Body request: RecuperacionRequest): Response<ApiResponse<Any>>

    @POST("api/auth/recuperacion/validar-token")
    suspend fun validarToken(@Body request: ValidacionCodigoRequest): Response<ApiResponse<Boolean>>

    @POST("api/auth/recuperacion/validar")
    suspend fun cambiarPassword(@Body request: RecuperacionValidacionRequest): Response<ApiResponse<Any>>
}