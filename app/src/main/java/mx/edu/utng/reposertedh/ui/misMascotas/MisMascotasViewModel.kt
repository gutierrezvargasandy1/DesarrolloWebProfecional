package mx.edu.utng.reposertedh.ui.misMascotas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.data.JwtDecoder
import mx.edu.utng.reposertedh.data.TokenManager
import mx.edu.utng.reposertedh.model.MascotaModel
import mx.edu.utng.reposertedh.model.MascotaModel.Mascota
import mx.edu.utng.reposertedh.network.MascotaApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

// ── Estados de la pantalla ─────────────────────────────────────────────────────
sealed class MisMascotasState {
    object Loading : MisMascotasState()
    data class Success(val mascotas: List<Mascota>) : MisMascotasState()
    data class Error(val message: String) : MisMascotasState()
}

// ── Estados de las acciones (crear, editar, eliminar) ─────────────────────────
sealed class MascotaActionState {
    object Idle : MascotaActionState()
    object Loading : MascotaActionState()
    object Success : MascotaActionState()
    data class Error(val message: String) : MascotaActionState()
}

// ── ViewModel principal ────────────────────────────────────────────────────────
class MisMascotasViewModel(
    private val tokenManager: TokenManager,
    private val mascotaApiService: MascotaApiService
) : ViewModel() {

    private val _state = MutableStateFlow<MisMascotasState>(MisMascotasState.Loading)
    val state: StateFlow<MisMascotasState> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<MascotaActionState>(MascotaActionState.Idle)
    val actionState: StateFlow<MascotaActionState> = _actionState.asStateFlow()

    // Cache local de mascotas
    private var allMascotas: List<Mascota> = emptyList()
    private var currentFilter: String? = null
    private var currentQuery: String = ""

    init {
        cargarMascotas()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: String → RequestBody texto plano
    // ─────────────────────────────────────────────────────────────────────────
    private fun String.toPlainRequestBody(): RequestBody =
        this.toRequestBody("text/plain".toMediaTypeOrNull())

    // ─────────────────────────────────────────────────────────────────────────
    // OBTENER USER ID DEL TOKEN
    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun getUserIdFromToken(): Int? {
        val token = tokenManager.getToken() ?: return null
        val userId = JwtDecoder.getUserId(token)
        println("🆔 User ID from token: $userId")
        return userId
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARGAR MASCOTAS
    // ─────────────────────────────────────────────────────────────────────────
    fun cargarMascotas() {
        viewModelScope.launch {
            _state.value = MisMascotasState.Loading

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrBlank()) {
                    _state.value = MisMascotasState.Error("No hay sesión activa")
                    return@launch
                }

                val userId = JwtDecoder.getUserId(token) ?: run {
                    _state.value = MisMascotasState.Error("Token inválido")
                    return@launch
                }

                println("📡 Cargando mascotas para usuario: $userId")
                val response = mascotaApiService.listarTodas()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == 200) {
                        val todasLasMascotas = body.data ?: emptyList()
                        allMascotas = todasLasMascotas.filter { it.id_usuario == userId }
                        println("📊 Mascotas cargadas: ${allMascotas.size}")
                        aplicarFiltros()
                    } else {
                        _state.value =
                            MisMascotasState.Error(body?.message ?: "Error al cargar mascotas")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("❌ Error al cargar mascotas: $errorBody")
                    _state.value = MisMascotasState.Error("Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                println("💥 Excepción cargando mascotas: ${e.message}")
                _state.value = MisMascotasState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AGREGAR MASCOTA
    // ─────────────────────────────────────────────────────────────────────────
    fun agregarMascota(request: MascotaModel.MascotaCreateRequest, fotoFile: File? = null) {
        viewModelScope.launch {
            _actionState.value = MascotaActionState.Loading
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrBlank()) {
                    _actionState.value = MascotaActionState.Error("No hay sesión activa")
                    return@launch
                }

                val userId = getUserIdFromToken() ?: run {
                    _actionState.value =
                        MascotaActionState.Error("No se pudo obtener el ID de usuario")
                    return@launch
                }

                val r = request.copy(id_usuario = userId)

                println("📤 Creando mascota para usuario: $userId")

                // ✅ Foto (opcional)
                val fotoPart = fotoFile?.let { file ->
                    println("📸 Foto: ${file.name} (${file.length()} bytes)")
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                        .let { MultipartBody.Part.createFormData("foto", file.name, it) }
                }

                val response = mascotaApiService.crearMascota(
                    idUsuario      = r.id_usuario.toString().toPlainRequestBody(),
                    especie        = r.especie.toPlainRequestBody(),
                    nombre         = r.nombre?.toPlainRequestBody(),
                    raza           = r.raza?.toPlainRequestBody(),
                    color          = r.color?.toPlainRequestBody(),
                    sexo           = r.sexo?.toPlainRequestBody(),
                    edad           = r.edad?.toString()?.toPlainRequestBody(),
                    estado         = r.estado?.toPlainRequestBody(),
                    descripcion    = r.descripcion?.toPlainRequestBody(),
                    direccionHogar = r.direccion_hogar?.toPlainRequestBody(),
                    latitudHogar   = r.latitud_hogar?.toString()?.toPlainRequestBody(),
                    longitudHogar  = r.longitud_hogar?.toString()?.toPlainRequestBody(),
                    foto           = fotoPart
                )

                println("📡 Código de respuesta: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == 201 || body?.status == 200) {
                        _actionState.value = MascotaActionState.Success
                        cargarMascotas()
                    } else {
                        _actionState.value =
                            MascotaActionState.Error(body?.message ?: "Error al crear mascota")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("❌ Error respuesta: $errorBody")
                    _actionState.value =
                        MascotaActionState.Error("Error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                println("💥 Excepción: ${e.message}")
                _actionState.value = MascotaActionState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EDITAR MASCOTA
    // ─────────────────────────────────────────────────────────────────────────
    fun editarMascota(
        idMascota: Int,
        request: MascotaModel.MascotaUpdateRequest,
        fotoFile: File? = null
    ) {
        viewModelScope.launch {
            _actionState.value = MascotaActionState.Loading

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrBlank()) {
                    _actionState.value = MascotaActionState.Error("No hay sesión activa")
                    return@launch
                }

                println("📝 Editando mascota ID: $idMascota")

                // ✅ Foto (opcional)
                val fotoPart = fotoFile?.let { file ->
                    println("📸 Foto: ${file.name} (${file.length()} bytes)")
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                        .let { MultipartBody.Part.createFormData("foto", file.name, it) }
                }

                val response = mascotaApiService.actualizarMascota(
                    id             = idMascota,
                    nombre         = request.nombre?.toPlainRequestBody(),
                    especie        = request.especie?.toPlainRequestBody(),
                    raza           = request.raza?.toPlainRequestBody(),
                    color          = request.color?.toPlainRequestBody(),
                    sexo           = request.sexo?.toPlainRequestBody(),
                    edad           = request.edad?.toString()?.toPlainRequestBody(),
                    estado         = request.estado?.toPlainRequestBody(),
                    descripcion    = request.descripcion?.toPlainRequestBody(),
                    direccionHogar = request.direccion_hogar?.toPlainRequestBody(),
                    latitudHogar   = request.latitud_hogar?.toString()?.toPlainRequestBody(),
                    longitudHogar  = request.longitud_hogar?.toString()?.toPlainRequestBody(),
                    foto           = fotoPart
                )

                println("📡 Código de respuesta: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    println("✅ Respuesta exitosa: $body")
                    if (body?.status == 200) {
                        _actionState.value = MascotaActionState.Success
                        cargarMascotas()
                    } else {
                        _actionState.value =
                            MascotaActionState.Error(body?.message ?: "Error al actualizar")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("❌ Error respuesta: $errorBody")
                    _actionState.value =
                        MascotaActionState.Error("Error del servidor: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                println("💥 Excepción: ${e.message}")
                e.printStackTrace()
                _actionState.value = MascotaActionState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ELIMINAR MASCOTA
    // ─────────────────────────────────────────────────────────────────────────
    fun eliminarMascota(idMascota: Int) {
        viewModelScope.launch {
            _actionState.value = MascotaActionState.Loading

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrBlank()) {
                    _actionState.value = MascotaActionState.Error("No hay sesión activa")
                    return@launch
                }

                println("🗑️ Eliminando mascota ID: $idMascota")

                val response = mascotaApiService.eliminarMascota(idMascota)

                println("📡 Código de respuesta: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    println("✅ Respuesta exitosa: $body")
                    if (body?.status == 200) {
                        _actionState.value = MascotaActionState.Success
                        cargarMascotas()
                    } else {
                        _actionState.value =
                            MascotaActionState.Error(body?.message ?: "Error al eliminar")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("❌ Error respuesta: $errorBody")
                    _actionState.value =
                        MascotaActionState.Error("Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                println("💥 Excepción: ${e.message}")
                _actionState.value = MascotaActionState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CAMBIAR ESTADO
    // ─────────────────────────────────────────────────────────────────────────
    fun cambiarEstado(idMascota: Int, nuevoEstado: String) {
        val request = MascotaModel.MascotaUpdateRequest(estado = nuevoEstado)
        editarMascota(idMascota, request, null)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILTRAR POR ESTADO
    // ─────────────────────────────────────────────────────────────────────────
    fun filtrarPorEstado(estado: String?) {
        currentFilter = estado
        aplicarFiltros()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUSCAR POR TEXTO
    // ─────────────────────────────────────────────────────────────────────────
    fun buscar(query: String) {
        currentQuery = query.lowercase().trim()
        aplicarFiltros()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // APLICAR FILTROS COMBINADOS
    // ─────────────────────────────────────────────────────────────────────────
    private fun aplicarFiltros() {
        var resultado = allMascotas

        if (!currentFilter.isNullOrBlank()) {
            resultado = resultado.filter { it.estado.equals(currentFilter, ignoreCase = true) }
        }

        if (currentQuery.isNotBlank()) {
            resultado = resultado.filter { mascota ->
                mascota.nombre?.lowercase()?.contains(currentQuery) == true ||
                        mascota.raza?.lowercase()?.contains(currentQuery) == true ||
                        mascota.color?.lowercase()?.contains(currentQuery) == true ||
                        mascota.especie.lowercase().contains(currentQuery)
            }
        }

        _state.value = MisMascotasState.Success(resultado)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESETEAR ESTADO DE ACCIÓN
    // ─────────────────────────────────────────────────────────────────────────
    fun resetActionState() {
        _actionState.value = MascotaActionState.Idle
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REFRESCAR MANUALMENTE
    // ─────────────────────────────────────────────────────────────────────────
    fun refresh() {
        cargarMascotas()
    }

    class MisMascotasViewModelFactory(
        private val tokenManager: TokenManager,
        private val mascotaApiService: MascotaApiService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MisMascotasViewModel(tokenManager, mascotaApiService) as T
    }
}