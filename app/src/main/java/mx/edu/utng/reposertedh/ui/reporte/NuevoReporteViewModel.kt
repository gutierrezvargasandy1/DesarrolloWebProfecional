package mx.edu.utng.reposertedh.ui.reporte

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.data.JwtDecoder
import mx.edu.utng.reposertedh.data.TokenManager
import mx.edu.utng.reposertedh.network.ReporteApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

// ── Estados ───────────────────────────────────────────────────────────────────
sealed class NuevoReporteState {
    object Idle    : NuevoReporteState()
    object Loading : NuevoReporteState()
    object Success : NuevoReporteState()
    data class Error(val message: String) : NuevoReporteState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────
class NuevoReporteViewModel(
    private val api: ReporteApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow<NuevoReporteState>(NuevoReporteState.Idle)
    val state: StateFlow<NuevoReporteState> = _state

    fun crearReporte(
        descripcion: String,
        lat: Double,
        lng: Double,
        idMascota: Int? = null,
        imagenFile: File? = null
    ) {
        viewModelScope.launch {
            _state.value = NuevoReporteState.Loading
            try {
                val token  = tokenManager.getToken() ?: ""
                val userId = JwtDecoder.getUserId(token) ?: run {
                    _state.value = NuevoReporteState.Error("Sesión expirada")
                    return@launch
                }

                // ── Multipart EXACTO como lo espera Flask ──────────────────────
                val builder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id_usuario", userId.toString())
                    .addFormDataPart("descripcion", descripcion)
                    .addFormDataPart("latitud", lat.toString())
                    .addFormDataPart("longitud", lng.toString())

                if (idMascota != null) {
                    builder.addFormDataPart("id_mascota", idMascota.toString())
                }

                val imagenPart: MultipartBody.Part? = imagenFile?.let { file ->
                    val comprimida = comprimirImagen(file)
                    MultipartBody.Part.createFormData(
                        "imagen",
                        comprimida.name,
                        comprimida.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                }

                val response = api.crearReporte(builder.build(), imagenPart)

                _state.value =
                    if (response.isSuccessful) NuevoReporteState.Success
                    else NuevoReporteState.Error("Error ${response.code()}: ${response.message()}")

            } catch (e: Exception) {
                _state.value = NuevoReporteState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    // ── Compresión optimizada ─────────────────────────────────────────────────
    fun comprimirImagen(archivo: File): File {
        val bitmap = BitmapFactory.decodeFile(archivo.absolutePath)
        val archivoComprimido = File(archivo.parent, "comprimida_${archivo.name}")
        val outputStream = FileOutputStream(archivoComprimido)

        val maxSize = 1024
        val ratio = minOf(
            maxSize.toFloat() / bitmap.width,
            maxSize.toFloat() / bitmap.height
        )

        val nuevoBitmap =
            if (ratio < 1f)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt(),
                    (bitmap.height * ratio).toInt(),
                    true
                )
            else bitmap

        nuevoBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.flush()
        outputStream.close()

        return archivoComprimido
    }
}

// ── Factory ───────────────────────────────────────────────────────────────────
class NuevoReporteViewModelFactory(
    private val api: ReporteApiService,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NuevoReporteViewModel(api, tokenManager) as T
    }
}