package mx.edu.utng.reposertedh.network


import mx.edu.utng.reposertedh.model.ApiResponse
import mx.edu.utng.reposertedh.model.MascotaModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface MascotaApiService {

    @GET("api/mascotas/")
    suspend fun listarTodas(): Response<ApiResponse<List<MascotaModel.Mascota>>>

    @GET("api/mascotas/{id}")
    suspend fun obtenerPorId(
        @Path("id") id: Int
    ): Response<ApiResponse<MascotaModel.Mascota>>

    // ✅ Campos individuales como el backend espera
    @Multipart
    @POST("api/mascotas/")
    suspend fun crearMascota(
        @Part("id_usuario") idUsuario: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("nombre") nombre: RequestBody?,
        @Part("raza") raza: RequestBody?,
        @Part("color") color: RequestBody?,
        @Part("sexo") sexo: RequestBody?,
        @Part("edad") edad: RequestBody?,
        @Part("estado") estado: RequestBody?,
        @Part("descripcion") descripcion: RequestBody?,
        @Part("direccion_hogar") direccionHogar: RequestBody?,
        @Part("latitud_hogar") latitudHogar: RequestBody?,
        @Part("longitud_hogar") longitudHogar: RequestBody?,
        @Part foto: MultipartBody.Part?
    ): Response<ApiResponse<Map<String, Int>>>

    // ✅ Igual para actualizar
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
        @Part("direccion_hogar") direccionHogar: RequestBody?,
        @Part("latitud_hogar") latitudHogar: RequestBody?,
        @Part("longitud_hogar") longitudHogar: RequestBody?,
        @Part foto: MultipartBody.Part?
    ): Response<ApiResponse<Map<String, Int>>>

    @DELETE("api/mascotas/{id}")
    suspend fun eliminarMascota(
        @Path("id") id: Int
    ): Response<ApiResponse<Boolean>>
}