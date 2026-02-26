package mx.edu.utng.reposertedh.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.model.ReporteResponse
import mx.edu.utng.reposertedh.network.ReporteApiService

sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    data class Success(val reportes: List<ReporteResponse>) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(private val api: ReporteApiService) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val state: StateFlow<DashboardState> = _state

    private var todosLosReportes: List<ReporteResponse> = emptyList()

    init {
        cargarReportes()
    }

    fun cargarReportes() {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            try {
                val response = api.listarTodos()
                if (response.isSuccessful) {
                    todosLosReportes = response.body()?.data ?: emptyList()
                    _state.value = DashboardState.Success(todosLosReportes)
                } else {
                    _state.value = DashboardState.Error("Error al cargar reportes")
                }
            } catch (e: Exception) {
                _state.value = DashboardState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    fun buscar(query: String) {
        val filtrados = if (query.isBlank()) {
            todosLosReportes
        } else {
            todosLosReportes.filter {
                it.titulo.contains(query, ignoreCase = true) ||
                        it.descripcion.contains(query, ignoreCase = true)
            }
        }
        _state.value = DashboardState.Success(filtrados)
    }

    fun filtrarRecientes() {
        val filtrados = todosLosReportes.sortedByDescending { it.fechaReporte }
        _state.value = DashboardState.Success(filtrados)
    }

    fun filtrarPorEstado(estado: String) {
        val filtrados = todosLosReportes.filter {
            it.estado.equals(estado, ignoreCase = true)
        }
        _state.value = DashboardState.Success(filtrados)
    }
}

class DashboardViewModelFactory(private val api: ReporteApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DashboardViewModel(api) as T
}