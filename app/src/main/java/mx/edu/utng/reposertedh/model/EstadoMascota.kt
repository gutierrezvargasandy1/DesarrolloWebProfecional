package mx.edu.utng.reposertedh.model

enum class EstadoMascota {

    PERDIDA,
    ENCONTRADA,
    NORMAL,
    CELO,
    ADOPCION;

    fun label(): String {
        return when (this) {
            PERDIDA -> "Mascota perdida"
            ENCONTRADA -> "Mascota encontrada"
            NORMAL -> "En casa"
            CELO -> "En celo"
            ADOPCION -> "En adopción"
        }
    }

    fun emoji(): String {
        return when (this) {
            PERDIDA -> "🔴"
            ENCONTRADA -> "🟢"
            NORMAL -> "🏠"
            CELO -> "💗"
            ADOPCION -> "💛"
        }
    }
}