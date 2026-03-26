package mx.edu.utng.reposertedh.model

import com.google.gson.annotations.SerializedName

// ======================= REQUEST =======================
// Coincide con CrearReporteDTO del backend
data class ReporteRequest(

    @SerializedName("id_usuario")
    val idUsuario: Int,

    @SerializedName("id_mascota")
    val idMascota: Int,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("latitud")
    val latitud: Double,

    @SerializedName("longitud")
    val longitud: Double,

    // Opcionales que el backend acepta
    @SerializedName("direccion")
    val direccion: String? = null,

    @SerializedName("estado")
    val estado: String = "PERDIDA"
)


// ======================= RESPONSE =======================
// Parseará EXACTO lo que manda tu backend
data class ReporteResponse(

    @SerializedName("id_reporte")
    val idReporte: Int,

    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val direccion: String?,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("fecha_reporte")
    val fechaReporte: String,

    val mascota: Mascota
) {
    // 🔥 Sigues usando estos campos igual que antes en tu app
    val titulo: String
        get() = mascota.nombre

    val imagenUrl: String?
        get() = mascota.fotoUrl
}


// ======================= MASCOTA ANIDADA =======================
data class Mascota(

    @SerializedName("id_mascota")
    val idMascota: Int,

    val nombre: String,
    val especie: String,
    val raza: String,
    val color: String,
    val sexo: String,
    val edad: Int,
    val descripcion: String,

    @SerializedName("foto_url")
    val fotoUrl: String?
)