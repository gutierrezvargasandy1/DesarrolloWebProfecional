package mx.edu.utng.reposertedh.network

import mx.edu.utng.reposertedh.model.ApiResponse
import mx.edu.utng.reposertedh.model.LoginRequest
import mx.edu.utng.reposertedh.model.LoginResponse
import mx.edu.utng.reposertedh.model.RecuperacionRequest
import mx.edu.utng.reposertedh.model.VerificarCodigoRequest
import mx.edu.utng.reposertedh.model.CambiarPasswordRequest
import mx.edu.utng.reposertedh.model.RegistroResponse
import mx.edu.utng.reposertedh.model.UsuarioRegistroRequest

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    // ─────────────────────────────
    // LOGIN
    // ─────────────────────────────
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>


    // ─────────────────────────────
    // REGISTRO
    // ─────────────────────────────
    @POST("api/usuarios/registro")
    suspend fun registro(
        @Body request: UsuarioRegistroRequest
    ): Response<ApiResponse<RegistroResponse>>


    // ─────────────────────────────
    // ENVIAR CODIGO RECUPERACION
    // ─────────────────────────────
    @POST("api/auth/recuperar")
    suspend fun enviarCodigo(
        @Body request: RecuperacionRequest
    ): Response<ApiResponse<Boolean>>


    // ─────────────────────────────
    // VERIFICAR CODIGO
    // ─────────────────────────────
    @POST("api/auth/verificar-codigo")
    suspend fun verificarCodigo(
        @Body request: VerificarCodigoRequest
    ): Response<ApiResponse<Boolean>>


    // ─────────────────────────────
    // CAMBIAR PASSWORD
    // ─────────────────────────────
    @POST("api/auth/cambiar-password")
    suspend fun cambiarPassword(
        @Body request: CambiarPasswordRequest
    ): Response<ApiResponse<Boolean>>
}