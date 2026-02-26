package mx.edu.utng.reposertedh.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.model.TipoUsuarioEnum
import mx.edu.utng.reposertedh.model.UsuarioRegistroRequest
import mx.edu.utng.reposertedh.network.AuthApiService

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel(private val api: AuthApiService) : ViewModel() {

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> = _state

    fun registrar(
        nombre: String,
        apellido: String,
        correo: String,
        telefono: String,
        password: String,
        tipoUsuario: TipoUsuarioEnum
    ) {
        viewModelScope.launch {
            _state.value = RegisterState.Loading
            try {
                val response = api.registro(
                    UsuarioRegistroRequest(nombre, apellido, correo, telefono, password, tipoUsuario)
                )
                if (response.isSuccessful && response.body()?.data?.registrado == true) {
                    _state.value = RegisterState.Success
                } else {
                    _state.value = RegisterState.Error(response.body()?.message ?: "Error al registrar")
                }
            } catch (e: Exception) {
                _state.value = RegisterState.Error("Sin conexión: ${e.message}")
            }
        }
    }
}

class RegisterViewModelFactory(private val api: AuthApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        RegisterViewModel(api) as T
}