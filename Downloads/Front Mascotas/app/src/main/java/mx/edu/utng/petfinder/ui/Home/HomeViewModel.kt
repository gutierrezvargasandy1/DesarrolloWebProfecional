package mx.edu.utng.petfinder.ui.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.petfinder.data.remote.ReportesModule.Dto.ReporteDto
import mx.edu.utng.petfinder.data.remote.ReportesModule.Service.ReporteApiService

class HomeViewModel(
    private val repository: ReporteRepository
) : ViewModel() {

    private val _reportes = MutableStateFlow<List<ReporteDto>>(emptyList())
    val reportes: StateFlow<List<ReporteDto>> = _reportes

    private val _filteredReportes = MutableStateFlow<List<ReporteDto>>(emptyList())
    val filteredReportes: StateFlow<List<ReporteDto>> = _filteredReportes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter: StateFlow<String?> = _selectedFilter

    init {
        cargarReportes()
    }

    fun cargarReportes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.getAllReportesFull()

            if (result.isNotEmpty()) {
                _reportes.value = result
                aplicarFiltros()
            } else {
                _error.value = "No se pudieron cargar los reportes"
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

        val filtrados = _reportes.value.filter { reporte ->
            // Filtro por búsqueda
            val matchSearch = if (query.isNotBlank()) {
                reporte.mascota.nombre?.lowercase()?.contains(query) == true ||
                        reporte.mascota.especie.lowercase().contains(query) ||
                        (reporte.mascota.raza?.lowercase()?.contains(query) ?: false)
            } else true

            // Filtro por estado
            val matchFilter = if (filter != null && filter != "TODOS") {
                reporte.estado.equals(filter, ignoreCase = true)
            } else true

            matchSearch && matchFilter
        }

        _filteredReportes.value = filtrados
    }

    fun obtenerReportePorId(id: Int): ReporteDto? {
        return _reportes.value.find { it.id_reporte == id }
    }
}

// Factory para crear el ViewModel
class HomeViewModelFactory(
    private val api: ReporteApiService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = ReporteRepository(api)

        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}