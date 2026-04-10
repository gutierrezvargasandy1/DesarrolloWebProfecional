package mx.edu.utng.petfinder.data.remote.ReportesModule.Dto

data class ReporteDto(
    val id_reporte: Int,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val direccion: String?,
    val estado: String,
    val fecha_reporte: String?,
    val mascota: MascotaSimpleDto
)

data class MascotaSimpleDto(
    val id_mascota: Int,
    val nombre: String?,
    val especie: String,
    val raza: String?,
    val color: String?,
    val sexo: String?,
    val edad: String?,
    val descripcion: String?,
    val foto_url: String?,
    val estado: String?,
    val direccion_hogar: String?,
    val latitud_hogar: Double?,
    val longitud_hogar: Double?
)

data class CrearReporteRequest(
    val id_mascota: Int,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val direccion: String? = null,
    val estado: String? = "PERDIDA"
)

data class ActualizarReporteRequest(
    val descripcion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val direccion: String?,
    val estado: String?
)