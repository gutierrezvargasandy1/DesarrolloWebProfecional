package mx.edu.utng.petfinder.data.remote.ReportesModule.Service

import mx.edu.utng.petfinder.data.remote.ConfigClient.ApiResponse
import mx.edu.utng.petfinder.data.remote.ReportesModule.Dto.ActualizarReporteRequest
import mx.edu.utng.petfinder.data.remote.ReportesModule.Dto.CrearReporteRequest
import mx.edu.utng.petfinder.data.remote.ReportesModule.Dto.ReporteDto
import retrofit2.http.*

interface ReporteApiService {

    // ───────── GET ALL USER ─────────
    @GET("api/reportes/")
    suspend fun getAll(): ApiResponse<List<Int>>

    // ───────── GET BY ID ─────────
    @GET("api/reportes/{id}")
    suspend fun getById(
        @Path("id") id: Int
    ): ApiResponse<Map<String, Int>>

    // ───────── PUBLIC ─────────
    @GET("api/reportes/public")
    suspend fun getAllPublic(): ApiResponse<List<ReporteDto>>

    @GET("api/reportes/public/{id}")
    suspend fun getByIdPublic(
        @Path("id") id: Int
    ): ApiResponse<ReporteDto>

    // ───────── FULL (CON MASCOTA) ─────────
    @GET("api/reportes/full")
    suspend fun getAllFull(): ApiResponse<List<ReporteDto>>

    @GET("api/reportes/{id}/full")
    suspend fun getByIdFull(
        @Path("id") id: Int
    ): ApiResponse<ReporteDto>

    // ───────── CREATE ─────────
    @POST("api/reportes/")
    suspend fun createReporte(
        @Body request: CrearReporteRequest
    ): ApiResponse<Map<String, Int>>

    // ───────── UPDATE ─────────
    @PUT("api/reportes/{id}")
    suspend fun updateReporte(
        @Path("id") id: Int,
        @Body request: ActualizarReporteRequest
    ): ApiResponse<Map<String, Int>>

    // ───────── DELETE ─────────
    @DELETE("api/reportes/{id}")
    suspend fun deleteReporte(
        @Path("id") id: Int
    ): ApiResponse<String>
}