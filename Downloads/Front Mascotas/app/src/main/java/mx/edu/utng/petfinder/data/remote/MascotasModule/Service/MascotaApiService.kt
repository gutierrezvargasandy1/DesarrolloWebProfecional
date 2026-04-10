package mx.edu.utng.petfinder.data.remote.MascotasModule.Service

import mx.edu.utng.petfinder.data.remote.ConfigClient.ApiResponse
import mx.edu.utng.petfinder.data.remote.MascotasModule.Dto.MascotaDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface MascotaApiService {

    // ───────────── CREATE ─────────────
    @Multipart
    @POST("api/mascotas/")
    suspend fun crearMascota(
        @Part("nombre") nombre: RequestBody?,
        @Part("especie") especie: RequestBody,
        @Part("raza") raza: RequestBody?,
        @Part("color") color: RequestBody?,
        @Part("sexo") sexo: RequestBody?,
        @Part("edad") edad: RequestBody?,
        @Part("estado") estado: RequestBody?,
        @Part("descripcion") descripcion: RequestBody?,
        @Part("direccion_hogar") direccion: RequestBody?,
        @Part("latitud_hogar") latitud: RequestBody?,
        @Part("longitud_hogar") longitud: RequestBody?,
        @Part foto: MultipartBody.Part?
    ): ApiResponse<Map<String, Int>>

    // ───────────── GET ALL ─────────────
    @GET("api/mascotas/")
    suspend fun obtenerMascotas(): ApiResponse<List<Any>>

    // ───────────── GET BY ID ─────────────
    @GET("api/mascotas/{id}")
    suspend fun obtenerMascota(
        @Path("id") id: Int
    ): ApiResponse<MascotaDto>

    // ───────────── UPDATE ─────────────
    @Multipart
    @PUT("api/mascotas/{id}")
    suspend fun actualizarMascota(
        @Path("id") id: Int,
        @Part("nombre") nombre: RequestBody?,
        @Part("especie") especie: RequestBody?,
        @Part("raza") raza: RequestBody?,
        @Part("color") color: RequestBody?,
        @Part("sexo") sexo: RequestBody?,
        @Part("edad") edad: RequestBody?,
        @Part("estado") estado: RequestBody?,
        @Part("descripcion") descripcion: RequestBody?,
        @Part("direccion_hogar") direccion: RequestBody?,
        @Part("latitud_hogar") latitud: RequestBody?,
        @Part("longitud_hogar") longitud: RequestBody?,
        @Part foto: MultipartBody.Part?
    ): ApiResponse<Map<String, Int>>

    // ───────────── DELETE ─────────────
    @DELETE("api/mascotas/{id}")
    suspend fun eliminarMascota(
        @Path("id") id: Int
    ): ApiResponse<Boolean>
}