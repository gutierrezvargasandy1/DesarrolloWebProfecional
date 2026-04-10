package mx.edu.utng.petfinder.data.remote.ConfigClient

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)