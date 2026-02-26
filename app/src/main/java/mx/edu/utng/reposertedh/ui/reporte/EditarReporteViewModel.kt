package mx.edu.utng.reposertedh.ui.reporte

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.model.ReporteResponse
import mx.edu.utng.reposertedh.network.ReporteApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class EditarState {
    object Idle : EditarState()
    object Loading : EditarState()
    object Success : EditarState()
    data class Error(val message: String) : EditarState()
    data class Loaded(val reporte: ReporteResponse) : EditarState()
}

class EditarReporteViewModel(private val api: ReporteApiService) : ViewModel() {

    private val _state = MutableStateFlow<EditarState>(EditarState.Idle)
    val state: StateFlow<EditarState> = _state

    fun cargarReporte(id: Int) {
        viewModelScope.launch {
            _state.value = EditarState.Loading
            try {
                val response = api.obtenerPorId(id)
                if (response.isSuccessful) {
                    val reporte = response.body()?.data as? ReporteResponse
                    if (reporte != null) _state.value = EditarState.Loaded(reporte)
                }
            } catch (e: Exception) {
                _state.value = EditarState.Error("Error al cargar datos")
            }
        }
    }

    // --- Lógica de Compresión ---
    fun comprimirImagen(context: Context, archivo: File): File {
        val bitmap = BitmapFactory.decodeFile(archivo.absolutePath)
        val archivoComprimido = File(context.cacheDir, "edit_comprimida_${archivo.name}")
        val outputStream = FileOutputStream(archivoComprimido)

        val maxSize = 1024
        val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
        val nuevoBitmap = if (ratio < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else bitmap

        nuevoBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.flush()
        outputStream.close()
        return archivoComprimido
    }

    fun actualizar(context: Context, id: Int, titulo: String, descripcion: String, imagenFile: File?) {
        viewModelScope.launch {
            _state.value = EditarState.Loading
            try {
                // 1. Preparar JSON del reporte
                val updateData = mapOf("titulo" to titulo, "descripcion" to descripcion)
                val json = Gson().toJson(updateData)
                val reporteBody = json.toRequestBody("application/json".toMediaType())

                // 2. Comprimir e imagen si existe
                val imagenPart = imagenFile?.let { file ->
                    val comprimida = comprimirImagen(context, file)
                    val imageBody = comprimida.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("imagen", comprimida.name, imageBody)
                }

                // 3. Petición al API
                val response = api.actualizarReporte(id, reporteBody, imagenPart)

                if (response.isSuccessful) {
                    _state.value = EditarState.Success
                } else {
                    _state.value = EditarState.Error("Error: ${response.code()}")
                }

            } catch (e: Exception) {
                _state.value = EditarState.Error("Error de red: ${e.message}")
            }
        }
    }
}

class EditarViewModelFactory(private val api: ReporteApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = EditarReporteViewModel(api) as T
}