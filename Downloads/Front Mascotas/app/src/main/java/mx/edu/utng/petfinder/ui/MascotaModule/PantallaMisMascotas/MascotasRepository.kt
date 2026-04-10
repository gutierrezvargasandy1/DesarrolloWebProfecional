package mx.edu.utng.petfinder.ui.MascotaModule.PantallaMisMascotas

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.edu.utng.petfinder.data.remote.MascotasModule.Dto.MascotaDto
import mx.edu.utng.petfinder.data.remote.MascotasModule.Service.MascotaApiService

class MascotasRepository(
    private val api: MascotaApiService
) {

    suspend fun obtenerMascotas(): List<MascotaDto> {
        return try {
            Log.d("MASCOTAS_DEBUG", "=== INICIANDO LLAMADA API ===")
            val response = api.obtenerMascotas()

            Log.d("MASCOTAS_DEBUG", "Response success: ${response.success}")
            Log.d("MASCOTAS_DEBUG", "Response message: ${response.message}")
            Log.d("MASCOTAS_DEBUG", "Response data type: ${response.data?.javaClass?.name}")

            // Convertir response.data a JSON y luego a lista de MascotaDto
            val gson = Gson()
            val jsonData = gson.toJson(response.data)
            Log.d("MASCOTAS_DEBUG", "JSON data: $jsonData")

            // Convertir JSON a lista de MascotaDto
            val type = object : TypeToken<List<MascotaDto>>() {}.type
            val mascotasList: List<MascotaDto> = gson.fromJson(jsonData, type)

            Log.d("MASCOTAS_DEBUG", "Mascotas encontradas: ${mascotasList.size}")
            mascotasList.forEachIndexed { index, mascota ->
                Log.d("MASCOTAS_DEBUG", "Mascota $index: ${mascota.nombre} - ${mascota.especie}")
            }

            if (response.success == true && mascotasList.isNotEmpty()) {
                mascotasList
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MASCOTAS_DEBUG", "❌ Excepción: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun obtenerMascota(id: Int): MascotaDto? {
        return try {
            val response = api.obtenerMascota(id)
            if (response.success == true && response.data != null) {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun eliminarMascota(id: Int): Boolean {
        return try {
            val response = api.eliminarMascota(id)
            response.success == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun verificarSiTieneReporte(mascotaId: Int): Boolean {
        return false
    }
}