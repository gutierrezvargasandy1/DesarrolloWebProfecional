package mx.edu.utng.petfinder.ui.MascotaModule.PantallaMisMascotas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.petfinder.data.remote.MascotasModule.Dto.MascotaDto
import mx.edu.utng.petfinder.data.remote.MascotasModule.Service.MascotaApiService

class MascotasViewModel(
    private val repository: MascotasRepository
) : ViewModel() {

    private val _mascotas = MutableStateFlow<List<MascotaDto>>(emptyList())
    val mascotas: StateFlow<List<MascotaDto>> = _mascotas

    private val _filteredMascotas = MutableStateFlow<List<MascotaDto>>(emptyList())
    val filteredMascotas: StateFlow<List<MascotaDto>> = _filteredMascotas

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter: StateFlow<String?> = _selectedFilter

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess

    init {
        Log.d("MASCOTAS_DEBUG", "=== ViewModel INICIALIZADO ===")
        cargarMascotas()
    }

    fun cargarMascotas() {
        Log.d("MASCOTAS_DEBUG", "cargarMascotas() llamado")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d("MASCOTAS_DEBUG", "Llamando a repository.obtenerMascotas()")

            val result = repository.obtenerMascotas()

            Log.d("MASCOTAS_DEBUG", "Resultado recibido: ${result.size} mascotas")

            if (result.isNotEmpty()) {
                _mascotas.value = result
                aplicarFiltros()
                Log.d("MASCOTAS_DEBUG", "✅ Mascotas cargadas: ${result.size}")
                result.forEach { mascota ->
                    Log.d("MASCOTAS_DEBUG", "  - ${mascota.nombre} (${mascota.especie})")
                }
            } else {
                val errorMsg = "No se encontraron mascotas"
                _error.value = errorMsg
                Log.e("MASCOTAS_DEBUG", "❌ $errorMsg")
            }

            _isLoading.value = false
        }
    }

    fun actualizarBusqueda(query: String) {
        _searchQuery.value = query
        aplicarFiltros()
    }

    fun actualizarFiltro(estado: String?) {
        _selectedFilter.value = estado
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val query = _searchQuery.value.lowercase()
        val filter = _selectedFilter.value

        val filtrados = _mascotas.value.filter { mascota ->
            val matchSearch = if (query.isNotBlank()) {
                mascota.nombre?.lowercase()?.contains(query) == true
            } else true

            val matchFilter = if (filter != null && filter != "TODOS") {
                mascota.estado.equals(filter, ignoreCase = true)
            } else true

            matchSearch && matchFilter
        }

        _filteredMascotas.value = filtrados
        Log.d("MASCOTAS_DEBUG", "Filtrados: ${filtrados.size} mascotas")
    }

    fun eliminarMascota(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.eliminarMascota(id)
            if (result) {
                _deleteSuccess.value = true
                cargarMascotas()
            } else {
                _error.value = "Error al eliminar la mascota"
            }
            _isLoading.value = false
        }
    }

    fun resetDeleteSuccess() {
        _deleteSuccess.value = false
    }

    fun verificarReporte(mascotaId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val tieneReporte = repository.verificarSiTieneReporte(mascotaId)
            callback(tieneReporte)
        }
    }
}

class MascotasViewModelFactory(
    private val api: MascotaApiService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d("MASCOTAS_DEBUG", "=== Creando ViewModel Factory ===")
        val repository = MascotasRepository(api)
        if (modelClass.isAssignableFrom(MascotasViewModel::class.java)) {
            return MascotasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}