package mx.edu.utng.petfinder.ui.Home

import mx.edu.utng.petfinder.data.remote.ReportesModule.Dto.ReporteDto
import mx.edu.utng.petfinder.data.remote.ReportesModule.Service.ReporteApiService

class ReporteRepository(
    private val api: ReporteApiService
) {

    suspend fun getAllReportesFull(): List<ReporteDto> {
        return try {
            val response = api.getAllFull()
            if (response.success == true && response.data != null) {
                response.data
            } else {
                throw Exception(response.message ?: "Error al cargar los reportes")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getReporteByIdFull(id: Int): ReporteDto? {
        return try {
            val response = api.getByIdFull(id)
            if (response.success == true && response.data != null) {
                response.data
            } else {
                throw Exception(response.message ?: "Error al cargar el reporte")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllReportesPublic(): List<ReporteDto> {
        return try {
            val response = api.getAllPublic()
            if (response.success == true && response.data != null) {
                response.data
            } else {
                throw Exception(response.message ?: "Error al cargar reportes públicos")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getReporteByIdPublic(id: Int): ReporteDto? {
        return try {
            val response = api.getByIdPublic(id)
            if (response.success == true && response.data != null) {
                response.data
            } else {
                throw Exception(response.message ?: "Error al cargar el reporte público")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}