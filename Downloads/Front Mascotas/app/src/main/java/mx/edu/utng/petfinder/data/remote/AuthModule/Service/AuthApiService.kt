package mx.edu.utng.petfinder.data.remote.AuthModule.Service

import mx.edu.utng.petfinder.data.remote.ConfigClient.ApiResponse
import mx.edu.utng.petfinder.data.remote.AuthModule.Dto.AuthDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    // ───────────────────────── LOGIN ─────────────────────────
    @POST("api/auth/login")
    suspend fun login(
        @Body request: AuthDto.LoginRequest
    ): ApiResponse<AuthDto.LoginResponse>


    // ─────────────── SOLICITAR RECUPERACIÓN ────────────────
    @POST("api/auth/recuperar")
    suspend fun recuperarPassword(
        @Body request: AuthDto.RecuperarPasswordRequest
    ): ApiResponse<Boolean>


    // ─────────────── VERIFICAR CÓDIGO ───────────────────────
    @POST("api/auth/verificar-codigo")
    suspend fun verificarCodigo(
        @Body request: AuthDto.VerificarCodigoRequest
    ): ApiResponse<Boolean>


    // ─────────────── CAMBIAR PASSWORD ───────────────────────
    @POST("api/auth/cambiar-password")
    suspend fun cambiarPassword(
        @Body request: AuthDto.CambiarPasswordRequest
    ): ApiResponse<Boolean>
}