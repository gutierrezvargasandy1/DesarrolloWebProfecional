package mx.edu.utng.petfinder.ui.AuthModule.PantallaLogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.petfinder.data.local.TokenManager
import mx.edu.utng.petfinder.data.remote.AuthModule.Service.AuthApiService

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    fun login(correo: String, password: String) {

        viewModelScope.launch {

            _loading.value = true
            _error.value = null
            _success.value = false

            val result = repository.login(correo, password)

            if (result) {
                _success.value = true
            } else {
                _error.value = "Credenciales incorrectas"
            }

            _loading.value = false
        }
    }
}

class LoginViewModelFactory(
    private val api: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val repository = AuthRepository(api, tokenManager)

        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}