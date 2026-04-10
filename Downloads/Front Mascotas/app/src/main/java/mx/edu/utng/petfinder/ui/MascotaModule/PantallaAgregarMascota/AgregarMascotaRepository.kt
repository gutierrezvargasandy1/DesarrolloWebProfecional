package mx.edu.utng.petfinder.ui.MascotaModule.PantallaAgregarMascota

import mx.edu.utng.petfinder.data.remote.MascotasModule.Service.MascotaApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AgregarMascotaRepository(
    private val api: MascotaApiService
) {

    suspend fun crearMascota(
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
    ): Boolean {
        return try {
            val nombreBody = nombre?.toRequestBody("text/plain".toMediaTypeOrNull())
            val especieBody = especie.toRequestBody("text/plain".toMediaTypeOrNull())
            val razaBody = raza?.toRequestBody("text/plain".toMediaTypeOrNull())
            val colorBody = color?.toRequestBody("text/plain".toMediaTypeOrNull())
            val sexoBody = sexo?.toRequestBody("text/plain".toMediaTypeOrNull())
            val edadBody = edad?.toRequestBody("text/plain".toMediaTypeOrNull())
            val estadoBody = estado.toRequestBody("text/plain".toMediaTypeOrNull())
            val descripcionBody = descripcion?.toRequestBody("text/plain".toMediaTypeOrNull())
            val direccionBody = direccion?.toRequestBody("text/plain".toMediaTypeOrNull())
            val latitudBody = latitud?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val longitudBody = longitud?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val fotoPart = fotoFile?.let {
                val requestFile = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("foto", it.name, requestFile)
            }

            val response = api.crearMascota(
                nombre = nombreBody,
                especie = especieBody,
                raza = razaBody,
                color = colorBody,
                sexo = sexoBody,
                edad = edadBody,
                estado = estadoBody,
                descripcion = descripcionBody,
                direccion = direccionBody,
                latitud = latitudBody,
                longitud = longitudBody,
                foto = fotoPart
            )

            response.success == true && response.data != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}