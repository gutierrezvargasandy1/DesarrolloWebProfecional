package mx.edu.utng.reposertedh.ui.reporte

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.data.TokenManager
import mx.edu.utng.reposertedh.model.ReporteResponse
import mx.edu.utng.reposertedh.network.ReporteApiService
import mx.edu.utng.reposertedh.ui.components.OsmMapView

class DetalleViewModel(private val api: ReporteApiService) : ViewModel() {
    private val _reporte = MutableStateFlow<ReporteResponse?>(null)
    val reporte: StateFlow<ReporteResponse?> = _reporte

    fun cargar(id: Int) {
        viewModelScope.launch {
            try {
                val response = api.obtenerPorId(id)
                if (response.isSuccessful) _reporte.value = response.body()?.data
            } catch (e: Exception) {
                Log.e("Detalle", "Error cargando reporte: ${e.message}")
            }
        }
    }
}

class DetalleViewModelFactory(private val api: ReporteApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DetalleViewModel(api) as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleReporteScreen(
    viewModel: DetalleViewModel,
    reporteId: Int,
    onBack: () -> Unit
) {
    val reporte by viewModel.reporte.collectAsState()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var token by remember { mutableStateOf<String?>(null) }

    // IP del servidor
    val baseUrlImagen = "http://100.89.122.81:8085"

    LaunchedEffect(Unit) {
        token = tokenManager.getToken()
        Log.d("Detalle", "Token recuperado: ${token != null}")
    }

    LaunchedEffect(reporteId) {
        viewModel.cargar(reporteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del reporte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        reporte?.let { r ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ── Imagen del Reporte ──────────────────────────────────────
                val imagenUrl = r.imagenUrl

                if (!imagenUrl.isNullOrBlank() && token != null) {

                    val path = if (imagenUrl.startsWith("/")) imagenUrl else "/$imagenUrl"
                    val fullUrl = "$baseUrlImagen$path"

                    Log.d("Detalle", "Cargando imagen desde: $fullUrl")

                    val imageRequest = ImageRequest.Builder(context)
                        .data(fullUrl)
                        .addHeader("Authorization", "Bearer $token")
                        .crossfade(true)
                        .build()

                    val painter = rememberAsyncImagePainter(
                        model = imageRequest,
                        onState = { state ->
                            if (state is AsyncImagePainter.State.Error) {
                                Log.e("Detalle", "Error Coil: ${state.result.throwable.message}")
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = "Foto del reporte",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (painter.state is AsyncImagePainter.State.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(30.dp))
                        }

                        if (painter.state is AsyncImagePainter.State.Error) {
                            Text("Error al cargar imagen", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                // ── Resto de la información ──────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(r.titulo, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                        Text(r.estado, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("📅 Fecha: ${r.fechaReporte.take(10)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

                Text("Descripción", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(r.descripcion, style = MaterialTheme.typography.bodyLarge)

                Spacer(Modifier.height(24.dp))

                Text("📍 Ubicación del incidente", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    OsmMapView(
                        latitud = r.latitud,
                        longitud = r.longitud,
                        titulo = r.titulo,
                        modifier = Modifier.fillMaxWidth().height(250.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}