package mx.edu.utng.reposertedh.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.data.TokenManager
import mx.edu.utng.reposertedh.model.LoginRequest
import mx.edu.utng.reposertedh.network.AuthApiService

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    private val api: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(correo: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val response = api.login(LoginRequest(correo, password))
                if (response.isSuccessful) {
                    val token = response.body()?.data?.token
                    if (!token.isNullOrEmpty()) {
                        tokenManager.saveToken(token)
                        _state.value = LoginState.Success(token)
                    } else {
                        _state.value = LoginState.Error("Token vacío")
                    }
                } else {
                    _state.value = LoginState.Error("Credenciales incorrectas")
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error("Sin conexión: ${e.message}")
            }
        }
    }
}

class LoginViewModelFactory(
    private val api: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LoginViewModel(api, tokenManager) as T
}