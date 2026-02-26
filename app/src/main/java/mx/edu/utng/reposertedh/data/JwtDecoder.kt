package mx.edu.utng.reposertedh.data

import android.util.Base64
import org.json.JSONObject

object JwtDecoder {

    fun getUserId(token: String): Int? {
        return try {

            val payload = token.split(".")[1]

            val decodedBytes = Base64.decode(
                payload,
                Base64.URL_SAFE
            )

            val json = JSONObject(String(decodedBytes))

            json.optInt("id")

        } catch (e: Exception) {
            null
        }
    }
}