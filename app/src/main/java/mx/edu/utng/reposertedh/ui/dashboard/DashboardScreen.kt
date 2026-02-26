package mx.edu.utng.reposertedh.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Logout // Importante para el icono de salida
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Info
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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.data.TokenManager
import mx.edu.utng.reposertedh.model.ReporteResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onReporteClick: (Int) -> Unit,
    onNuevoReporte: () -> Unit,
    onMisReportes: () -> Unit,
    onLogout: () -> Unit // Nueva navegación al Login
) {
    var query by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("TODOS") }
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    // Para manejar el borrado del token
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reportes Ciudadanos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // Botón de Cerrar Sesión
                    IconButton(onClick = {
                        scope.launch {
                            tokenManager.clearToken() // Borra el JWT
                            onLogout() // Regresa al Login
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onMisReportes) {
                        Text("Mis Reportes")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoReporte,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Reportar") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sección de búsqueda y filtros
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.buscar(it)
                        },
                        placeholder = { Text("Buscar por título...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Filtros con Scroll Horizontal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filtros = listOf("TODOS", "RECIENTES", "ENVIADO", "EN PROCESO", "RESUELTO")
                        filtros.forEach { filtro ->
                            FilterChip(
                                selected = filtroSeleccionado == filtro,
                                onClick = {
                                    filtroSeleccionado = filtro
                                    when (filtro) {
                                        "TODOS" -> viewModel.cargarReportes()
                                        "RECIENTES" -> viewModel.filtrarRecientes()
                                        else -> viewModel.filtrarPorEstado(filtro)
                                    }
                                },
                                label = { Text(filtro) }
                            )
                        }
                    }
                }
            }

            // Lista de reportes
            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    is DashboardState.Loading -> {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                    is DashboardState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.error)
                            Text((state as DashboardState.Error).message)
                        }
                    }
                    is DashboardState.Success -> {
                        val reportes = (state as DashboardState.Success).reportes
                        if (reportes.isEmpty()) {
                            Text("No se encontraron reportes", Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(reportes) { reporte ->
                                    ReporteItemCard(
                                        reporte = reporte,
                                        onClick = { onReporteClick(reporte.idReporte) }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ReporteItemCard(reporte: ReporteResponse, onClick: () -> Unit) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var token by remember { mutableStateOf<String?>(null) }
    val baseUrl = "http://100.89.122.81:8085"

    LaunchedEffect(Unit) {
        token = tokenManager.getToken()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min)
        ) {
            // Imagen miniatura
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
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = reporte.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        EstadoBadge(reporte.estado)
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = reporte.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Event,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = reporte.fechaReporte.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun EstadoBadge(estado: String) {
    val (color, label) = when (estado.uppercase()) {
        "ENVIADO" -> MaterialTheme.colorScheme.primary to "Enviado"
        "EN PROCESO" -> MaterialTheme.colorScheme.tertiary to "En proceso"
        "RESUELTO" -> MaterialTheme.colorScheme.secondary to "Resuelto"
        else -> MaterialTheme.colorScheme.outline to estado
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}