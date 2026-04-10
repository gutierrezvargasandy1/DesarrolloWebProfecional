package mx.edu.utng.petfinder.ui.AuthModule.PantallaLogin

import mx.edu.utng.petfinder.data.local.TokenManager
import mx.edu.utng.petfinder.data.remote.AuthModule.Dto.AuthDto
import mx.edu.utng.petfinder.data.remote.AuthModule.Service.AuthApiService

class AuthRepository(
    private val api: AuthApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(correo: String, password: String): Boolean {

        return try {

            val response = api.login(
                AuthDto.LoginRequest(correo, password)
            )

            val token = response.data?.token
                ?: throw Exception("Token no recibido del servidor")

            tokenManager.saveToken(token)

            true

        } catch (e: Exception) {

            e.printStackTrace()
            false

        }
    }
}