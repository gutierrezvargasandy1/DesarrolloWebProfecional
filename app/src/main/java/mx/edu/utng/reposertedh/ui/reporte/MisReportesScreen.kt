package mx.edu.utng.reposertedh.ui.reporte

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.data.TokenManager
import mx.edu.utng.reposertedh.model.ReporteResponse
import mx.edu.utng.reposertedh.network.ReporteApiService

// --- VIEWMODEL ---
class MisReportesViewModel(
    private val api: ReporteApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _reportes = MutableStateFlow<List<ReporteResponse>>(emptyList())
    val reportes: StateFlow<List<ReporteResponse>> = _reportes

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun cargar() {
        viewModelScope.launch {
            _loading.value = true
            try {
                // El backend filtra por el usuario del token internamente o tú manejas la lógica
                val response = api.listarTodos()
                if (response.isSuccessful) {
                    _reportes.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión"
            } finally {
                _loading.value = false
            }
        }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            try {
                api.eliminarReporte(id)
                cargar() // Recargar lista tras eliminar con éxito
            } catch (_: Exception) {}
        }
    }
}

class MisReportesViewModelFactory(
    private val api: ReporteApiService,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MisReportesViewModel(api, tokenManager) as T
}

// --- SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReportesScreen(
    viewModel: MisReportesViewModel,
    onEditarReporte: (Int) -> Unit, // <--- Este callback dispara la navegación
    onBack: () -> Unit
) {
    val reportes by viewModel.reportes.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var reporteAEliminar by remember { mutableStateOf<ReporteResponse?>(null) }

    // Cargar reportes al entrar a la pantalla
    LaunchedEffect(Unit) { viewModel.cargar() }

    // Diálogo de Confirmación para Eliminar
    reporteAEliminar?.let { reporte ->
        AlertDialog(
            onDismissRequest = { reporteAEliminar = null },
            icon = { Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Eliminar reporte?") },
            text = {
                Text("¿Estás seguro de que deseas eliminar \"${reporte.titulo}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminar(reporte.idReporte)
                        reporteAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { reporteAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Reportes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (reportes.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No tienes reportes creados", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reportes) { reporte ->
                        MiReporteCard(
                            reporte = reporte,
                            // Al pulsar editar, mandamos el ID a la función de navegación
                            onEdit = { onEditarReporte(reporte.idReporte) },
                            onDelete = { reporteAEliminar = reporte }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiReporteCard(
    reporte: ReporteResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var token by remember { mutableStateOf<String?>(null) }
    val baseUrl = "http://100.89.122.81:8085"

    LaunchedEffect(Unit) {
        token = tokenManager.getToken()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen miniatura con Token
            if (!reporte.imagenUrl.isNullOrBlank() && token != null) {
                val request = ImageRequest.Builder(context)
                    .data("$baseUrl${reporte.imagenUrl}")
                    .setHeader("Authorization", "Bearer $token")
                    .crossfade(true)
                    .build()

                Image(
                    painter = rememberAsyncImagePainter(request),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reporte.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = reporte.estado,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = if (reporte.fechaReporte.length >= 10) reporte.fechaReporte.take(10) else reporte.fechaReporte,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Botones de acción: Editar y Eliminar
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}