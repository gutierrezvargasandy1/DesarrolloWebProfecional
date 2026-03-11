package mx.edu.utng.reposertedh.model

class MascotaModel {

    // ─────────────────────────────
    // CREAR MASCOTA
    // ─────────────────────────────
    data class MascotaCreateRequest(
        val id_usuario: Int,
        val especie: String,
        val nombre: String? = null,
        val raza: String? = null,
        val color: String? = null,
        val sexo: String? = null,
        val edad: Int? = null,
        val estado: String? = "NORMAL",
        val descripcion: String? = null,
        val direccion_hogar: String? = null,
        val latitud_hogar: Double? = null,
        val longitud_hogar: Double? = null
    )

    // ─────────────────────────────
    // ACTUALIZAR MASCOTA
    // ─────────────────────────────
    data class MascotaUpdateRequest(
        val nombre: String? = null,
        val especie: String? = null,
        val raza: String? = null,
        val color: String? = null,
        val sexo: String? = null,
        val edad: Int? = null,
        val estado: String? = null,
        val descripcion: String? = null,
        val direccion_hogar: String? = null,
        val latitud_hogar: Double? = null,
        val longitud_hogar: Double? = null
    )

    // ─────────────────────────────
    // RESPUESTA MASCOTA
    // ─────────────────────────────
    data class Mascota(
        val id_mascota: Int,
        val id_usuario: Int,
        val nombre: String?,
        val especie: String,
        val raza: String?,
        val color: String?,
        val sexo: String?,
        val edad: Int?,
        val estado: String,
        val descripcion: String?,
        val foto_url: String?,
        val direccion_hogar: String?,
        val latitud_hogar: Double?,
        val longitud_hogar: Double?,
        val fecha_registro: String
    )

    // ─────────────────────────────
    // RESPUESTA API GENERAL
    // ─────────────────────────────
    data class ApiResponse<T>(
        val status: Int,
        val message: String,
        val data: T?
    )

    companion object {
        val adapter = MascotaModelAdapter
    }
}

// ── Extension functions para convertir requests a JSON ─────────────────────────
object MascotaModelAdapter {

    fun createRequestJson(request: MascotaModel.MascotaCreateRequest): String {
        val json = StringBuilder("{")

        // Agregar espacios después de las comas para mejor legibilidad
        json.append("\"id_usuario\": ${request.id_usuario},")
        json.append("\"especie\": \"${request.especie}\"")

        request.nombre?.let {
            if (it.isNotBlank()) json.append(",\"nombre\": \"$it\"")
        }
        request.raza?.let {
            if (it.isNotBlank()) json.append(",\"raza\": \"$it\"")
        }
        request.color?.let {
            if (it.isNotBlank()) json.append(",\"color\": \"$it\"")
        }
        request.sexo?.let {
            if (it.isNotBlank()) json.append(",\"sexo\": \"$it\"")
        }
        request.edad?.let {
            json.append(",\"edad\": $it")
        }
        request.estado?.let {
            json.append(",\"estado\": \"$it\"")
        }
        request.descripcion?.let {
            if (it.isNotBlank()) json.append(",\"descripcion\": \"${escapeJson(it)}\"")
        }
        request.direccion_hogar?.let {
            if (it.isNotBlank()) json.append(",\"direccion_hogar\": \"${escapeJson(it)}\"")
        }
        request.latitud_hogar?.let {
            json.append(",\"latitud_hogar\": $it")
        }
        request.longitud_hogar?.let {
            json.append(",\"longitud_hogar\": $it")
        }

        json.append("}")

        // Log para debugging
        println("📦 JSON CREATE: $json")

        return json.toString()
    }

    fun createUpdateRequestJson(request: MascotaModel.MascotaUpdateRequest): String {
        val json = StringBuilder("{")
        val fields = mutableListOf<String>()

        request.nombre?.takeIf { it.isNotBlank() }?.let {
            fields.add("\"nombre\": \"$it\"")
        }
        request.especie?.takeIf { it.isNotBlank() }?.let {
            fields.add("\"especie\": \"$it\"")
        }
        request.raza?.takeIf { it.isNotBlank() }?.let {
            fields.add("\"raza\": \"$it\"")
        }
        request.color?.takeIf { it.isNotBlank() }?.let {
            fields.add("\"color\": \"$it\"")
        }
        request.sexo?.takeIf { it.isNotBlank() }?.let {
            fields.add("\"sexo\": \"$it\"")
        }
        request.edad?.let {
            fields.add("\"edad\": $it")
        }
        request.estado?.takeIf { it.isNotBlank() }?.let {
            fields.add("\"estado\": \"$it\"")
        }
        request.descripcion?.takeIf { it.isNotBlank() }?.let {
            fields.add("\"descripcion\": \"${escapeJson(it)}\"")
        }
        request.direccion_hogar?.takeIf { it.isNotBlank() }?.let {
            fields.add("\"direccion_hogar\": \"${escapeJson(it)}\"")
        }
        request.latitud_hogar?.let {
            fields.add("\"latitud_hogar\": $it")
        }
        request.longitud_hogar?.let {
            fields.add("\"longitud_hogar\": $it")
        }

        json.append(fields.joinToString(","))
        json.append("}")

        // Log para debugging
        println("📦 JSON UPDATE: $json")

        return json.toString()
    }

    // Función para escapar caracteres especiales en JSON
    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}