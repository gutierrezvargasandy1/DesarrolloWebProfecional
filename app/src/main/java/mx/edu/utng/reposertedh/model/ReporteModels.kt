package mx.edu.utng.reposertedh.model

import java.math.BigDecimal

data class ReporteRequest(
    val idUsuario: Int,
    val titulo: String,
    val descripcion: String,
    val latitud: BigDecimal,
    val longitud: BigDecimal,
)

data class ReporteResponse(
    val idReporte: Int,
    val titulo: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val imagenUrl: String?,
    val estado: String,
    val fechaReporte: String
)