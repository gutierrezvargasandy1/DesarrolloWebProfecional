package mx.edu.utng.petfinder.utils

object ImageHelper {
    private const val BASE_URL = "http://192.168.1.102:5000/"

    fun getFullImageUrl(photoUrl: String?): String? {
        if (photoUrl.isNullOrBlank()) return null
        return if (photoUrl.startsWith("http")) {
            photoUrl
        } else {
            BASE_URL + photoUrl.removePrefix("/")
        }
    }
}