package mx.edu.utng.reposertedh.ui.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.model.RecuperacionRequest
import mx.edu.utng.reposertedh.model.RecuperacionValidacionRequest
import mx.edu.utng.reposertedh.model.ValidacionCodigoRequest
import mx.edu.utng.reposertedh.network.AuthApiService

sealed class RecoveryState {
    object Idle : RecoveryState()
    object Loading : RecoveryState()
    object CodigoEnviado : RecoveryState()
    object CodigoValido : RecoveryState()
    object PasswordCambiado : RecoveryState()
    data class Error(val message: String) : RecoveryState()
}

class RecoveryViewModel(private val api: AuthApiService) : ViewModel() {

    private val _state = MutableStateFlow<RecoveryState>(RecoveryState.Idle)
    val state: StateFlow<RecoveryState> = _state

    // Guardamos correo y código internamente
    var correoGuardado: String = ""
        private set
    var codigoGuardado: String = ""
        private set

    // PASO 1: Enviar código al correo
    fun enviarCodigo(correo: String) {
        viewModelScope.launch {
            _state.value = RecoveryState.Loading
            try {
                val response = api.enviarCodigo(RecuperacionRequest(correo))
                if (response.isSuccessful) {
                    correoGuardado = correo  // guardamos el correo
                    _state.value = RecoveryState.CodigoEnviado
                } else {
                    _state.value = RecoveryState.Error("Correo no encontrado")
                }
            } catch (e: Exception) {
                _state.value = RecoveryState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    // PASO 2: Validar código
    fun validarCodigo(codigo: String) {
        viewModelScope.launch {
            _state.value = RecoveryState.Loading
            try {
                val response = api.validarToken(
                    ValidacionCodigoRequest(codigo, correoGuardado)
                )
                if (response.isSuccessful && response.body()?.data == true) {
                    codigoGuardado = codigo  // guardamos el código
                    _state.value = RecoveryState.CodigoValido
                } else {
                    _state.value = RecoveryState.Error("Código inválido o expirado")
                }
            } catch (e: Exception) {
                _state.value = RecoveryState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    // PASO 3: Cambiar contraseña
    fun cambiarPassword(nuevaPassword: String) {
        viewModelScope.launch {
            _state.value = RecoveryState.Loading
            try {
                val response = api.cambiarPassword(
                    RecuperacionValidacionRequest(correoGuardado, nuevaPassword, codigoGuardado)
                )
                if (response.isSuccessful) {
                    _state.value = RecoveryState.PasswordCambiado
                } else {
                    _state.value = RecoveryState.Error("Error al cambiar contraseña")
                }
            } catch (e: Exception) {
                _state.value = RecoveryState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    fun resetState() {
        _state.value = RecoveryState.Idle
    }
}

class RecoveryViewModelFactory(private val api: AuthApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        RecoveryViewModel(api) as T
}