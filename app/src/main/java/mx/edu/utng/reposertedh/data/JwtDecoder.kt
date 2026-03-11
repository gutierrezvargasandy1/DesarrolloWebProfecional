package mx.edu.utng.reposertedh.data

import android.util.Base64
import org.json.JSONObject

object JwtDecoder {

    fun getUserId(token: String): Int? {
        return try {
            // El token viene en formato: header.payload.signature
            val parts = token.split(".")
            if (parts.size < 2) return null

            val payload = parts[1]

            // Decodificar Base64 URL-safe
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val jsonString = String(decodedBytes)
            val json = JSONObject(jsonString)

            // El backend usa "user_id" en el token (según generate_token)
            // También puede venir como "sub" (subject) en algunos casos
            when {
                json.has("user_id") -> json.getInt("user_id")
                json.has("sub") -> json.getInt("sub")
                json.has("id") -> json.getInt("id")
                else -> null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Función para debug - muestra el contenido del token
    fun debugToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return "Token inválido"

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            String(decodedBytes)
        } catch (e: Exception) {
            "Error decodificando: ${e.message}"
        }
    }
}