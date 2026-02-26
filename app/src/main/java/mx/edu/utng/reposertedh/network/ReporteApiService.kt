package mx.edu.utng.reposertedh.network

import mx.edu.utng.reposertedh.model.ApiResponse
import mx.edu.utng.reposertedh.model.ReporteRequest
import mx.edu.utng.reposertedh.model.ReporteResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ReporteApiService {

    @GET("api/reportes")
    suspend fun listarTodos(): Response<ApiResponse<List<ReporteResponse>>>

    @GET("api/reportes/{id}")
    suspend fun obtenerPorId(@Path("id") id: Int): Response<ApiResponse<ReporteResponse>>

    @Multipart
    @POST("api/reportes/crear")
    suspend fun crearReporte(
        @Part("reporte") reporte: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): Response<ReporteResponse>

    @Multipart
    @PUT("api/reportes/{id}")
    suspend fun actualizarReporte(
        @Path("id") id: Int,
        @Part("reporte") reporte: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): Response<ReporteResponse> // O el DTO que manejes para la respuesta

    @DELETE("api/reportes/{id}")
    suspend fun eliminarReporte(@Path("id") id: Int): Response<ApiResponse<Any>>
}