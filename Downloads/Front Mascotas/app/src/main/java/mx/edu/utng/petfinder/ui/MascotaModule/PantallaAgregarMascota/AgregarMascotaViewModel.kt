package mx.edu.utng.petfinder.ui.MascotaModule.PantallaAgregarMascota

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.petfinder.data.remote.MascotasModule.Service.MascotaApiService
import java.io.File

class AgregarMascotaViewModel(
    private val repository: AgregarMascotaRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation: StateFlow<Pair<Double, Double>?> = _currentLocation

    private val _currentAddress = MutableStateFlow("")
    val currentAddress: StateFlow<String> = _currentAddress

    fun obtenerUbicacionActual() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val location = locationRepository.getCurrentLocation()
                if (location != null) {
                    _currentLocation.value = Pair(location.latitude, location.longitude)
                    val address = locationRepository.getAddressFromLocation(location.latitude, location.longitude)
                    _currentAddress.value = address
                } else {
                    _error.value = "No se pudo obtener la ubicación"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    fun buscarDireccion(direccion: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val coordinates = locationRepository.getCoordinatesFromAddress(direccion)
                if (coordinates != null) {
                    _currentLocation.value = coordinates
                    _currentAddress.value = direccion
                } else {
                    _error.value = "No se encontró la dirección"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    fun crearMascota(
        nombre: String?,
        especie: String,
        raza: String?,
        color: String?,
        sexo: String?,
        edad: String?,
        estado: String,
        descripcion: String?,
        direccion: String?,
        latitud: Double?,
        longitud: Double?,
        fotoFile: File?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.crearMascota(
                nombre = nombre,
                especie = especie,
                raza = raza,
                color = color,
                sexo = sexo,
                edad = edad,
                estado = estado,
                descripcion = descripcion,
                direccion = direccion,
                latitud = latitud,
                longitud = longitud,
                fotoFile = fotoFile
            )

            if (result) {
                _success.value = true
            } else {
                _error.value = "Error al crear la mascota"
            }

            _isLoading.value = false
        }
    }

    fun resetSuccess() {
        _success.value = false
    }
}

class AgregarMascotaViewModelFactory(
    private val api: MascotaApiService,
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = AgregarMascotaRepository(api)
        if (modelClass.isAssignableFrom(AgregarMascotaViewModel::class.java)) {
            return AgregarMascotaViewModel(repository, locationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}