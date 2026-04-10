package mx.edu.utng.petfinder.data.remote.MascotasModule.Dto

data class MascotaDto(
    val id_mascota: Int? = null,
    val nombre: String?,
    val especie: String,
    val raza: String?,
    val color: String?,
    val sexo: String?,
    val edad: String?,
    val estado: String?,
    val descripcion: String?,
    val foto_url: String?,
    val direccion_hogar: String?,
    val latitud_hogar: Double?,
    val longitud_hogar: Double?,
    val fecha_registro: String?
)

// Los DTOS no se usan pero los dejo  como modelo de Referencia del objeto
data class CreateMascotaRequest(
    val nombre: String?,
    val especie: String,
    val raza: String?,
    val color: String?,
    val sexo: String?,
    val edad: String?,
    val estado: String?,
    val descripcion: String?,
    val direccion_hogar: String?,
    val latitud_hogar: String?,
    val longitud_hogar: String?
)

data class UpdateMascotaRequest(
    val nombre: String?,
    val especie: String?,
    val raza: String?,
    val color: String?,
    val sexo: String?,
    val edad: String?,
    val estado: String?,
    val descripcion: String?,
    val direccion_hogar: String?,
    val latitud_hogar: String?,
    val longitud_hogar: String?
)