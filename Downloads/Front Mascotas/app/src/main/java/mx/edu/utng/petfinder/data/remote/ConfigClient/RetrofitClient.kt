package mx.edu.utng.petfinder.data.remote.ConfigClient

import mx.edu.utng.petfinder.data.local.TokenManager
import mx.edu.utng.petfinder.utils.JwtInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

     const val BASE_URL = "http://192.168.1.102:5000/"

    fun create(tokenManager: TokenManager): Retrofit {

        val client = OkHttpClient.Builder()
            .addInterceptor(JwtInterceptor(tokenManager))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}