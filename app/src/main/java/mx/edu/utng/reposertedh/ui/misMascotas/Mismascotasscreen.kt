package mx.edu.utng.reposertedh.ui.misMascotas

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import mx.edu.utng.reposertedh.data.TokenManager
import mx.edu.utng.reposertedh.model.MascotaModel
import mx.edu.utng.reposertedh.model.MascotaModel.Mascota
import mx.edu.utng.reposertedh.network.MascotaApiService
import mx.edu.utng.reposertedh.ui.components.OsmMapView
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.FileOutputStream
import java.net.URL

// ── Paleta PetFinder ───────────────────────────────────────────────────────────
private val PetOrange = Color(0xFFFF6B35)
private val PetOrangeLight = Color(0xFFFF8C5A)
private val PetYellow = Color(0xFFFFD166)
private val PetCream = Color(0xFFFFF8F0)
private val PetBrown = Color(0xFF6B3F1F)
private val PetGreen = Color(0xFF06D6A0)
private val PetRed = Color(0xFFEF5350)
private val PetBlue = Color(0xFF42A5F5)
private val PetPurple = Color(0xFFAB47BC)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF2D1B0E)
private val TextSecondary = Color(0xFF8B6347)
private val BgTop = Color(0xFFFFEDD5)

private val estadoColors = mapOf(
    "PERDIDA" to Triple(PetRed.copy(0.12f), PetRed, "🔴"),
    "ENCONTRADA" to Triple(PetGreen.copy(0.15f), Color(0xFF065F46), "🟢"),
    "NORMAL" to Triple(PetBlue.copy(0.12f), PetBlue, "🏠"),
    "CELO" to Triple(PetYellow.copy(0.25f), Color(0xFFB8860B), "💛")
)

// ── Modelo para sugerencia de dirección ───────────────────────────────────────
data class DireccionSugerencia(
    val displayName: String,
    val lat: Double,
    val lon: Double
)

// ── Enum de estados (para UI) ──────────────────────────────────────────────────
enum class EstadoMascotaUI(val dbValue: String, val label: String, val emoji: String) {
    PERDIDA("PERDIDA", "Perdida", "🔴"),
    ENCONTRADA("ENCONTRADA", "Encontrada", "🟢"),
    NORMAL("NORMAL", "Normal", "🏠"),
    CELO("CELO", "En celo", "💛");

    companion object {
        fun fromDbValue(value: String): EstadoMascotaUI {
            return values().find { it.dbValue == value.uppercase() } ?: NORMAL
        }
    }
}

// ── Pantalla principal ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisMascotasScreen(
    viewModel: MisMascotasViewModel,
    onBack: () -> Unit,
    onNuevoReporte: (idMascota: Int) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var filtroEstado by remember { mutableStateOf("TODOS") }
    var showAddDialog by remember { mutableStateOf(false) }
    var mascotaEditar by remember { mutableStateOf<Mascota?>(null) }
    var mascotaEliminar by remember { mutableStateOf<Mascota?>(null) }
    var mascotaEstado by remember { mutableStateOf<Mascota?>(null) }
    // Confirmación especial cuando el estado nuevo es PERDIDA
    var mascotaConfirmarPerdida by remember { mutableStateOf<Pair<Mascota, String>?>(null) }

    val state by viewModel.state.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(actionState) {
        when (actionState) {
            is MascotaActionState.Success -> {
                snackbarHostState.showSnackbar("✅ Cambios guardados")
                viewModel.resetActionState()
            }
            is MascotaActionState.Error -> {
                snackbarHostState.showSnackbar("⚠️ ${(actionState as MascotaActionState.Error).message}")
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BgTop,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = PetBrown,
                    contentColor = SurfaceWhite,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        topBar = { MascotasTopBar(onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PetOrange,
                contentColor = SurfaceWhite,
                shape = CircleShape,
                modifier = Modifier.shadow(12.dp, CircleShape)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(BgTop, Color(0xFFFFF3E0), PetCream)))
        ) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                drawCircle(PetOrange.copy(0.05f), 260.dp.toPx(), Offset(size.width * 0.9f, 0f))
                drawCircle(PetYellow.copy(0.07f), 180.dp.toPx(), Offset(0f, size.height * 0.85f))
            }

            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; viewModel.buscar(it) },
                    placeholder = { Text("Buscar por nombre, raza, color...", fontSize = 13.sp, color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PetOrange) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = ""; viewModel.buscar("") }) {
                                Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PetOrange,
                        unfocusedBorderColor = Color(0xFFE0C8B0),
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        cursorColor = PetOrange
                    )
                )

                val estadoChips = listOf("TODOS", "PERDIDA", "ENCONTRADA", "NORMAL", "CELO")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(estadoChips) { chip ->
                        val isSelected = filtroEstado == chip
                        val (_, chipLabel, chipEmoji) = estadoChipInfo(chip)
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(50)).clickable {
                                filtroEstado = chip
                                viewModel.filtrarPorEstado(if (chip == "TODOS") null else chip)
                            },
                            color = if (isSelected) PetOrange else SurfaceWhite,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, if (isSelected) PetOrange else Color(0xFFE0C8B0))
                        ) {
                            Text(
                                "$chipEmoji $chipLabel",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SurfaceWhite else TextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                if (state is MisMascotasState.Success) {
                    val count = (state as MisMascotasState.Success).mascotas.size
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🐾", fontSize = 13.sp)
                        Text(
                            "$count mascota${if (count != 1) "s" else ""} registrada${if (count != 1) "s" else ""}",
                            fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (state) {
                        is MisMascotasState.Loading -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = PetOrange)
                                Text("Cargando tus mascotas...", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                        is MisMascotasState.Error -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("😿", fontSize = 48.sp)
                                Text("Algo salió mal", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                                Text((state as MisMascotasState.Error).message, color = TextSecondary, fontSize = 13.sp)
                                Button(
                                    onClick = { viewModel.cargarMascotas() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PetOrange),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Reintentar") }
                            }
                        }
                        is MisMascotasState.Success -> {
                            val mascotas = (state as MisMascotasState.Success).mascotas
                            if (mascotas.isEmpty()) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("🐾", fontSize = 60.sp)
                                    Text(
                                        if (query.isNotEmpty()) "Sin resultados para \"$query\""
                                        else "Aún no tienes mascotas registradas",
                                        fontWeight = FontWeight.Bold, color = TextPrimary,
                                        fontSize = 15.sp, textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Toca el botón + para agregar tu primera mascota",
                                        color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(mascotas, key = { it.id_mascota }) { mascota ->
                                        MascotaCard(
                                            mascota = mascota,
                                            onEditar = { mascotaEditar = mascota },
                                            onEliminar = { mascotaEliminar = mascota },
                                            onCambiarEstado = { mascotaEstado = mascota }
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

    if (showAddDialog) {
        MascotaFormDialog(
            titulo = "Nueva Mascota",
            mascotaInicial = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { request, fotoFile ->
                when (request) {
                    is MascotaModel.MascotaCreateRequest -> viewModel.agregarMascota(request, fotoFile)
                }
                showAddDialog = false
            }
        )
    }

    mascotaEditar?.let { mascota ->
        MascotaFormDialog(
            titulo = "Editar Mascota",
            mascotaInicial = mascota,
            onDismiss = { mascotaEditar = null },
            onConfirm = { request, fotoFile ->
                when (request) {
                    is MascotaModel.MascotaUpdateRequest -> {
                        val nuevoEstado = request.estado ?: mascota.estado
                        if (nuevoEstado.uppercase() == "PERDIDA" && mascota.estado.uppercase() != "PERDIDA") {
                            // Guardar cambios primero y luego preguntar si crear reporte
                            viewModel.editarMascota(mascota.id_mascota, request, fotoFile)
                            mascotaConfirmarPerdida = Pair(mascota.copy(estado = "PERDIDA"), "PERDIDA")
                        } else {
                            viewModel.editarMascota(mascota.id_mascota, request, fotoFile)
                        }
                    }
                }
                mascotaEditar = null
            }
        )
    }

    mascotaEliminar?.let { mascota ->
        EliminarDialog(
            nombre = mascota.nombre ?: "Mascota",
            onDismiss = { mascotaEliminar = null },
            onConfirm = {
                viewModel.eliminarMascota(mascota.id_mascota)
                mascotaEliminar = null
            }
        )
    }

    mascotaEstado?.let { mascota ->
        CambiarEstadoDialog(
            mascota = mascota,
            onDismiss = { mascotaEstado = null },
            onConfirm = { nuevoEstado ->
                viewModel.cambiarEstado(mascota.id_mascota, nuevoEstado)
                mascotaEstado = null
                if (nuevoEstado.uppercase() == "PERDIDA" && mascota.estado.uppercase() != "PERDIDA") {
                    mascotaConfirmarPerdida = Pair(mascota.copy(estado = "PERDIDA"), nuevoEstado)
                }
            }
        )
    }

    // ── Diálogo: Confirmar crear reporte cuando estado = PERDIDA ──────────
    mascotaConfirmarPerdida?.let { (mascota, _) ->
        Dialog(onDismissRequest = { mascotaConfirmarPerdida = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(68.dp).background(PetRed.copy(0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("🔴", fontSize = 34.sp) }
                    Text(
                        "¿Crear reporte de búsqueda?",
                        fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary, textAlign = TextAlign.Center
                    )
                    Text(
                        "${mascota.nombre ?: "Tu mascota"} fue marcada como perdida.\n¿Quieres publicar un reporte para que la comunidad te ayude a encontrarla?",
                        fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { mascotaConfirmarPerdida = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0C8B0))
                        ) { Text("Ahora no", color = TextSecondary, fontSize = 13.sp) }
                        Button(
                            onClick = {
                                val id = mascota.id_mascota
                                mascotaConfirmarPerdida = null
                                onNuevoReporte(id)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PetRed)
                        ) { Text("Crear reporte", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    }
                }
            }
        }
    }
}

// ── Top Bar ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MascotasTopBar(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(BgTop, BgTop)))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = PetBrown) }
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🐾", fontSize = 20.sp)
                    Text("Mis Mascotas", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PetBrown)
                }
                Text("Gestiona y reporta el estado de tus mascotas", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

// ── Card de mascota ────────────────────────────────────────────────────────────
@Composable
private fun MascotaCard(
    mascota: Mascota,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onCambiarEstado: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var token by remember { mutableStateOf<String?>(null) }
    val baseUrl = "http://100.89.122.81:8085"

    LaunchedEffect(Unit) { token = tokenManager.getToken() }

    val estadoKey = mascota.estado.uppercase()
    val (bgColor, txtColor, emoji) = estadoColors[estadoKey]
        ?: Triple(Color(0xFFEEEEEE), TextSecondary, "•")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            colors = listOf((bgColor as Color).copy(alpha = 0.6f), Color(0xFFFFEDD5))
                        )
                    )
                )
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(88.dp).shadow(6.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFEDD5)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!mascota.foto_url.isNullOrBlank() && token != null) {
                            val req = ImageRequest.Builder(context)
                                .data("$baseUrl${mascota.foto_url}")
                                .setHeader("Authorization", "Bearer $token")
                                .crossfade(true).build()
                            androidx.compose.foundation.Image(
                                painter = rememberAsyncImagePainter(req),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(text = especieEmoji(mascota.especie), fontSize = 40.sp)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            mascota.nombre ?: "Sin nombre", fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold, color = TextPrimary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text("${mascota.especie} · ${mascota.raza ?: "Sin raza"}", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                        if (!mascota.color.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Outlined.Palette, null, modifier = Modifier.size(12.dp), tint = TextSecondary)
                                Text(mascota.color, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        mascota.edad?.let { edad ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                                Icon(Icons.Outlined.Cake, null, modifier = Modifier.size(12.dp), tint = TextSecondary)
                                Text("$edad año${if (edad != 1) "s" else ""}", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.clickable { onCambiarEstado() },
                        color = bgColor,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji as String, fontSize = 16.sp)
                            Text(EstadoMascotaUI.fromDbValue(mascota.estado).label, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = txtColor as Color)
                        }
                    }
                }
            }

            if (!mascota.descripcion.isNullOrBlank()) {
                Text(
                    mascota.descripcion, fontSize = 12.sp, color = TextSecondary,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Mini mapa si tiene coordenadas
            if (mascota.latitud_hogar != null && mascota.longitud_hogar != null) {
                HorizontalDivider(color = Color(0xFFF0E0D0), thickness = 1.dp)
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    OsmMapView(
                        latitud = mascota.latitud_hogar,
                        longitud = mascota.longitud_hogar,
                        titulo = mascota.nombre ?: "Hogar",
                        modifier = Modifier.fillMaxSize()
                    )
                    // Etiqueta encima del mapa
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
                        color = PetOrange.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Outlined.Home, null, tint = SurfaceWhite, modifier = Modifier.size(10.dp))
                            Text("Hogar", fontSize = 9.sp, color = SurfaceWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0E0D0), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(icon = Icons.Default.SwapHoriz, label = "Estado", color = PetPurple, onClick = onCambiarEstado)
                ActionButton(icon = Icons.Default.Edit, label = "Editar", color = PetBlue, onClick = onEditar)
                ActionButton(icon = Icons.Default.Delete, label = "Eliminar", color = PetRed, onClick = onEliminar)
            }
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    TextButton(onClick = onClick, colors = ButtonDefaults.textButtonColors(contentColor = color)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Diálogo: Agregar / Editar ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MascotaFormDialog(
    titulo: String,
    mascotaInicial: Mascota?,
    onDismiss: () -> Unit,
    onConfirm: (Any, File?) -> Unit
) {
    var nombre by remember { mutableStateOf(mascotaInicial?.nombre ?: "") }
    var especie by remember { mutableStateOf(mascotaInicial?.especie ?: "") }
    var raza by remember { mutableStateOf(mascotaInicial?.raza ?: "") }
    var color by remember { mutableStateOf(mascotaInicial?.color ?: "") }
    var edad by remember { mutableStateOf(mascotaInicial?.edad?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(mascotaInicial?.descripcion ?: "") }
    var estado by remember { mutableStateOf(mascotaInicial?.estado ?: "NORMAL") }
    var sexo by remember { mutableStateOf(mascotaInicial?.sexo ?: "") }

    // ── Campos de ubicación ────────────────────────────────────────────────
    var direccionQuery by remember { mutableStateOf(mascotaInicial?.direccion_hogar ?: "") }
    var selectedLat by remember { mutableStateOf(mascotaInicial?.latitud_hogar) }
    var selectedLon by remember { mutableStateOf(mascotaInicial?.longitud_hogar) }
    var sugerencias by remember { mutableStateOf<List<DireccionSugerencia>>(emptyList()) }
    var showSugerencias by remember { mutableStateOf(false) }
    var buscandoDireccion by remember { mutableStateOf(false) }
    var ubicacionConfirmada by remember { mutableStateOf(mascotaInicial?.latitud_hogar != null) }

    var especieExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }
    var sexoExpanded by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var token by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) { token = tokenManager.getToken() }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val file = File(context.cacheDir, "mascota_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out -> inputStream?.copyTo(out) }
                selectedImageFile = file
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // ── Función de búsqueda de dirección con Nominatim ─────────────────────
    fun buscarDireccion(query: String) {
        if (query.length < 3) { sugerencias = emptyList(); showSugerencias = false; return }
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(500) // debounce
            buscandoDireccion = true
            try {
                val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=5&addressdetails=1"
                // ✅ Dispatchers.IO para llamadas de red
                val json = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val conn = URL(url).openConnection()
                    conn.setRequestProperty("User-Agent", "MisMascotasApp/1.0")
                    conn.connectTimeout = 8000
                    conn.readTimeout = 8000
                    conn.getInputStream().bufferedReader().readText()
                }
                val arr = JSONArray(json)
                val lista = mutableListOf<DireccionSugerencia>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    lista.add(
                        DireccionSugerencia(
                            displayName = obj.getString("display_name"),
                            lat = obj.getString("lat").toDouble(),
                            lon = obj.getString("lon").toDouble()
                        )
                    )
                }
                sugerencias = lista
                showSugerencias = lista.isNotEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
                sugerencias = emptyList()
                showSugerencias = false
            } finally {
                buscandoDireccion = false
            }
        }
    }

    val especies = listOf("Perro", "Gato", "Conejo", "Ave", "Reptil", "Otro")
    val sexos = listOf("Macho", "Hembra", "Desconocido")
    val isEditing = mascotaInicial != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(44.dp).background(
                            Brush.radialGradient(listOf(PetOrangeLight, PetOrange)), CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) { Text(especieEmoji(especie), fontSize = 22.sp) }
                    Column {
                        Text(titulo, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text("Completa los datos de tu mascota", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                HorizontalDivider(color = Color(0xFFF0E0D0))

                // Selector de imagen
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                            .background(Color(0xFFFFEDD5)).clickable { launcher.launch("image/*") }
                            .border(2.dp, PetOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImageUri != null -> {
                                androidx.compose.foundation.Image(
                                    painter = rememberAsyncImagePainter(selectedImageUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            isEditing && !mascotaInicial?.foto_url.isNullOrBlank() -> {
                                if (token != null) {
                                    val req = ImageRequest.Builder(context)
                                        .data("http://100.89.122.81:8085${mascotaInicial?.foto_url}")
                                        .setHeader("Authorization", "Bearer $token").crossfade(true).build()
                                    androidx.compose.foundation.Image(
                                        painter = rememberAsyncImagePainter(req),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else { Text(especieEmoji(especie), fontSize = 40.sp) }
                            }
                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, tint = PetOrange, modifier = Modifier.size(30.dp), contentDescription = null)
                                    Text("Foto", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                    Text(
                        text = when {
                            selectedImageUri != null -> "✓ Imagen seleccionada"
                            isEditing && !mascotaInicial?.foto_url.isNullOrBlank() -> "✓ Tiene foto"
                            else -> "Toca para agregar foto"
                        },
                        fontSize = 10.sp,
                        color = when {
                            selectedImageUri != null -> PetGreen
                            isEditing && !mascotaInicial?.foto_url.isNullOrBlank() -> PetBlue
                            else -> TextSecondary
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                PetFormField(value = nombre, onValueChange = { nombre = it }, label = "Nombre *", icon = Icons.Default.Pets)

                // Especie
                ExposedDropdownMenuBox(expanded = especieExpanded, onExpandedChange = { especieExpanded = it }) {
                    OutlinedTextField(
                        value = especie, onValueChange = {}, readOnly = true,
                        label = { Text("Especie *", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Pets, null, tint = PetOrange) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = especieExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(14.dp), colors = petFieldColors()
                    )
                    ExposedDropdownMenu(expanded = especieExpanded, onDismissRequest = { especieExpanded = false }) {
                        especies.forEach { e ->
                            DropdownMenuItem(text = { Text("${especieEmoji(e)} $e") }, onClick = { especie = e; especieExpanded = false })
                        }
                    }
                }

                PetFormField(value = raza, onValueChange = { raza = it }, label = "Raza", icon = Icons.Outlined.Info)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) { PetFormField(value = color, onValueChange = { color = it }, label = "Color", icon = Icons.Outlined.Palette) }
                    Box(Modifier.weight(1f)) { PetFormField(value = edad, onValueChange = { edad = it }, label = "Edad (años)", icon = Icons.Outlined.Cake, keyboardType = KeyboardType.Number) }
                }

                // Sexo
                ExposedDropdownMenuBox(expanded = sexoExpanded, onExpandedChange = { sexoExpanded = it }) {
                    OutlinedTextField(
                        value = sexo, onValueChange = {}, readOnly = true,
                        label = { Text("Sexo", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Wc, null, tint = PetOrange) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexoExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(14.dp), colors = petFieldColors()
                    )
                    ExposedDropdownMenu(expanded = sexoExpanded, onDismissRequest = { sexoExpanded = false }) {
                        sexos.forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { sexo = s; sexoExpanded = false }) }
                    }
                }

                // Estado
                ExposedDropdownMenuBox(expanded = estadoExpanded, onExpandedChange = { estadoExpanded = it }) {
                    val estadoInfo = estadoColors[estado.uppercase()]
                    OutlinedTextField(
                        value = "${estadoInfo?.third ?: "•"} ${EstadoMascotaUI.fromDbValue(estado).label}",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Estado *", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.SwapHoriz, null, tint = PetOrange) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(14.dp), colors = petFieldColors()
                    )
                    ExposedDropdownMenu(expanded = estadoExpanded, onDismissRequest = { estadoExpanded = false }) {
                        EstadoMascotaUI.values().forEach { e ->
                            DropdownMenuItem(text = { Text("${e.emoji} ${e.label}") }, onClick = { estado = e.dbValue; estadoExpanded = false })
                        }
                    }
                }

                // ── SECCIÓN DE UBICACIÓN ───────────────────────────────────
                HorizontalDivider(color = Color(0xFFF0E0D0))

                var obtenendoGps by remember { mutableStateOf(false) }
                var gpsError by remember { mutableStateOf<String?>(null) }

                // Verificar si ya tiene permisos al abrir el dialog
                val yaTienePermiso = remember {
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                            androidx.core.content.ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                }

                fun obtenerUbicacion() {
                    obtenendoGps = true
                    gpsError = null
                    scope.launch {
                        try {
                            @SuppressLint("MissingPermission")
                            val fusedClient = LocationServices.getFusedLocationProviderClient(context)

                            // Intentar lastLocation primero
                            val lastLoc: android.location.Location? =
                                kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                                    fusedClient.lastLocation
                                        .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc, null) }
                                        .addOnFailureListener { if (cont.isActive) cont.resume(null, null) }
                                }

                            val loc: android.location.Location? = if (lastLoc != null) {
                                lastLoc
                            } else {
                                // Fallback: getCurrentLocation fuerza lectura fresca del GPS
                                kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                                    val request = com.google.android.gms.location.CurrentLocationRequest.Builder()
                                        .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                                        .setDurationMillis(10_000)
                                        .setMaxUpdateAgeMillis(0)
                                        .build()
                                    fusedClient.getCurrentLocation(request, null)
                                        .addOnSuccessListener { l: android.location.Location? ->
                                            if (cont.isActive) cont.resume(l, null)
                                        }
                                        .addOnFailureListener {
                                            if (cont.isActive) cont.resume(null, null)
                                        }
                                }
                            }

                            if (loc != null) {
                                selectedLat = loc.latitude
                                selectedLon = loc.longitude
                                ubicacionConfirmada = true
                                // Geocodificación inversa
                                try {
                                    val url = "https://nominatim.openstreetmap.org/reverse?lat=${loc.latitude}&lon=${loc.longitude}&format=json"
                                    val json = withContext(Dispatchers.IO) {
                                        val conn = URL(url).openConnection()
                                        conn.setRequestProperty("User-Agent", "MisMascotasApp/1.0")
                                        conn.connectTimeout = 6000; conn.readTimeout = 6000
                                        conn.getInputStream().bufferedReader().readText()
                                    }
                                    val obj = org.json.JSONObject(json)
                                    direccionQuery = obj.optString("display_name", "Mi ubicación actual")
                                } catch (_: Exception) {
                                    direccionQuery = "%.5f, %.5f".format(loc.latitude, loc.longitude)
                                }
                            } else {
                                gpsError = "No se pudo obtener la ubicación. Activa el GPS del dispositivo."
                            }
                        } catch (e: Exception) {
                            gpsError = "Error GPS: ${e.message}"
                        } finally {
                            obtenendoGps = false
                        }
                    }
                }

                val gpsLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permisos ->
                    val tienePermiso = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (tienePermiso) obtenerUbicacion()
                    else gpsError = "Permiso de ubicación denegado"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.LocationOn, null, tint = PetOrange, modifier = Modifier.size(16.dp))
                        Text("Ubicación del hogar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    // Botón GPS
                    Surface(
                        onClick = {
                            if (yaTienePermiso) {
                                obtenerUbicacion() // Ya tiene permiso, obtener directo
                            } else {
                                gpsLauncher.launch(arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ))
                            }
                        },
                        color = if (obtenendoGps) PetOrange.copy(0.2f) else PetOrange.copy(0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (obtenendoGps) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = PetOrange, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.MyLocation, null, tint = PetOrange, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                if (obtenendoGps) "Buscando..." else "Usar GPS",
                                fontSize = 11.sp, color = PetOrange, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Mensaje de error GPS
                if (gpsError != null) {
                    Surface(color = PetRed.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = PetRed, modifier = Modifier.size(14.dp))
                            Text(gpsError!!, fontSize = 11.sp, color = PetRed)
                        }
                    }
                }

                // Campo de búsqueda de dirección (solo calle/colonia, Nominatim infiere la ciudad por coordenadas)
                Column {
                    OutlinedTextField(
                        value = direccionQuery,
                        onValueChange = { value ->
                            direccionQuery = value
                            ubicacionConfirmada = false
                            buscarDireccion(value)
                        },
                        label = { Text("Buscar calle, colonia...", fontSize = 12.sp) },
                        leadingIcon = {
                            if (buscandoDireccion) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PetOrange, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Search, null, tint = PetOrange)
                            }
                        },
                        trailingIcon = {
                            if (ubicacionConfirmada) {
                                Icon(Icons.Default.CheckCircle, null, tint = PetGreen, modifier = Modifier.size(20.dp))
                            } else if (direccionQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    direccionQuery = ""; selectedLat = null; selectedLon = null
                                    ubicacionConfirmada = false; sugerencias = emptyList(); showSugerencias = false
                                }) { Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (ubicacionConfirmada) PetGreen else PetOrange,
                            unfocusedBorderColor = if (ubicacionConfirmada) PetGreen.copy(0.5f) else Color(0xFFE0C8B0),
                            focusedContainerColor = Color(0xFFFFFAF5),
                            unfocusedContainerColor = Color(0xFFFFFAF5),
                            cursorColor = PetOrange
                        )
                    )

                    // Lista de sugerencias
                    AnimatedVisibility(visible = showSugerencias && sugerencias.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column {
                                sugerencias.forEachIndexed { index, sug ->
                                    if (index > 0) HorizontalDivider(color = Color(0xFFF5EBE0), thickness = 0.5.dp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            direccionQuery = sug.displayName
                                            selectedLat = sug.lat; selectedLon = sug.lon
                                            ubicacionConfirmada = true; showSugerencias = false; sugerencias = emptyList()
                                        }.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Outlined.LocationOn, null, tint = PetOrange, modifier = Modifier.size(16.dp))
                                        Text(sug.displayName, fontSize = 12.sp, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Mapa OSMDroid embebido — funciona en Dialog con AndroidView directo ──
                AnimatedVisibility(
                    visible = selectedLat != null && selectedLon != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    selectedLat?.let { lat ->
                        selectedLon?.let { lon ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Chips de coordenadas
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(color = PetOrange.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Outlined.PinDrop, null, tint = PetOrange, modifier = Modifier.size(12.dp))
                                            Text("%.5f".format(lat), fontSize = 11.sp, color = PetBrown, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Surface(color = PetOrange.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Outlined.PinDrop, null, tint = PetOrange, modifier = Modifier.size(12.dp))
                                            Text("%.5f".format(lon), fontSize = 11.sp, color = PetBrown, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier.fillMaxWidth().height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(2.dp, PetOrange.copy(0.3f), RoundedCornerShape(16.dp))
                                ) {
                                    // ✅ AndroidView directo — OSMDroid sí funciona en Dialog así
                                    key(lat, lon) {
                                        AndroidView(
                                            modifier = Modifier.fillMaxSize(),
                                            factory = { ctx ->
                                                Configuration.getInstance().userAgentValue = ctx.packageName
                                                MapView(ctx).apply {
                                                    setTileSource(TileSourceFactory.MAPNIK)
                                                    setMultiTouchControls(false)
                                                    isClickable = false
                                                    val punto = GeoPoint(lat, lon)
                                                    controller.setZoom(16.0)
                                                    controller.setCenter(punto)
                                                    val marker = Marker(this)
                                                    marker.position = punto
                                                    marker.title = nombre.ifBlank { "Hogar" }
                                                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                    overlays.add(marker)
                                                }
                                            },
                                            update = { mapView ->
                                                val punto = GeoPoint(lat, lon)
                                                mapView.overlays.clear()
                                                val marker = Marker(mapView)
                                                marker.position = punto
                                                marker.title = nombre.ifBlank { "Hogar" }
                                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                mapView.overlays.add(marker)
                                                mapView.controller.setZoom(16.0)
                                                mapView.controller.setCenter(punto)
                                                mapView.invalidate()
                                            }
                                        )
                                    }
                                    Surface(
                                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                                        color = PetGreen.copy(alpha = 0.92f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.CheckCircle, null, tint = SurfaceWhite, modifier = Modifier.size(10.dp))
                                            Text("Ubicación confirmada", fontSize = 9.sp, color = SurfaceWhite, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Descripción
                OutlinedTextField(
                    value = descripcion, onValueChange = { descripcion = it },
                    label = { Text("Descripción", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Description, null, tint = PetOrange) },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3,
                    shape = RoundedCornerShape(14.dp), colors = petFieldColors()
                )

                // Botones
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0xFFE0C8B0))
                    ) { Text("Cancelar", color = TextSecondary) }

                    Button(
                        onClick = {
                            if (nombre.isBlank() || especie.isBlank()) return@Button
                            if (isEditing) {
                                onConfirm(
                                    MascotaModel.MascotaUpdateRequest(
                                        nombre = nombre.trim().takeIf { it.isNotBlank() },
                                        especie = especie.trim().takeIf { it.isNotBlank() },
                                        raza = raza.trim().takeIf { it.isNotBlank() },
                                        color = color.trim().takeIf { it.isNotBlank() },
                                        sexo = sexo.trim().takeIf { it.isNotBlank() },
                                        edad = edad.toIntOrNull(),
                                        estado = estado,
                                        descripcion = descripcion.trim().takeIf { it.isNotBlank() },
                                        direccion_hogar = direccionQuery.trim().takeIf { it.isNotBlank() },
                                        latitud_hogar = selectedLat,
                                        longitud_hogar = selectedLon
                                    ), selectedImageFile
                                )
                            } else {
                                onConfirm(
                                    MascotaModel.MascotaCreateRequest(
                                        id_usuario = 0,
                                        especie = especie.trim(),
                                        nombre = nombre.trim(),
                                        raza = raza.trim().takeIf { it.isNotBlank() },
                                        color = color.trim().takeIf { it.isNotBlank() },
                                        sexo = sexo.trim().takeIf { it.isNotBlank() },
                                        edad = edad.toIntOrNull(),
                                        estado = estado,
                                        descripcion = descripcion.trim().takeIf { it.isNotBlank() },
                                        direccion_hogar = direccionQuery.trim().takeIf { it.isNotBlank() },
                                        latitud_hogar = selectedLat,
                                        longitud_hogar = selectedLon
                                    ), selectedImageFile
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PetOrange)
                    ) { Text("Guardar", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// ── Diálogo: Cambiar estado ────────────────────────────────────────────────────
@Composable
private fun CambiarEstadoDialog(mascota: Mascota, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var estadoSeleccionado by remember { mutableStateOf(mascota.estado) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Cambiar estado", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("¿Cuál es el estado actual de ${mascota.nombre ?: "tu mascota"}?", fontSize = 13.sp, color = TextSecondary)

                EstadoMascotaUI.values().forEach { e ->
                    val isSelected = estadoSeleccionado.uppercase() == e.dbValue
                    val (bgCol, txtCol, emojiStr) = estadoColors[e.dbValue] ?: Triple(Color(0xFFEEEEEE), TextSecondary, "•")
                    Surface(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { estadoSeleccionado = e.dbValue },
                        color = if (isSelected) (bgCol as Color).copy(alpha = 0.4f) else Color(0xFFF8F0E8),
                        shape = RoundedCornerShape(14.dp),
                        border = if (isSelected) BorderStroke(2.dp, txtCol as Color) else null
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(emojiStr as String, fontSize = 22.sp)
                            Column(Modifier.weight(1f)) {
                                Text(e.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(estadoDescripcion(e), fontSize = 11.sp, color = TextSecondary)
                            }
                            if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = txtCol as Color, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0xFFE0C8B0))) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    Button(onClick = { onConfirm(estadoSeleccionado) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = PetOrange)) {
                        Text("Confirmar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Diálogo: Eliminar ──────────────────────────────────────────────────────────
@Composable
private fun EliminarDialog(nombre: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(64.dp).background(PetRed.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Text("🗑️", fontSize = 30.sp) }
                Text("¿Eliminar mascota?", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("Esto eliminará permanentemente el registro de $nombre. Esta acción no se puede deshacer.", fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0xFFE0C8B0))) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = PetRed)) {
                        Text("Eliminar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────
@Composable
private fun PetFormField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, null, tint = PetOrange) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        shape = RoundedCornerShape(14.dp), colors = petFieldColors()
    )
}

@Composable
private fun petFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PetOrange, unfocusedBorderColor = Color(0xFFE0C8B0),
    focusedLabelColor = PetOrange, unfocusedLabelColor = TextSecondary,
    cursorColor = PetOrange, focusedContainerColor = Color(0xFFFFFAF5),
    unfocusedContainerColor = Color(0xFFFFFAF5)
)

private fun especieEmoji(especie: String): String = when (especie.lowercase()) {
    "perro", "can" -> "🐕"
    "gato", "gata" -> "🐈"
    "conejo" -> "🐰"
    "ave", "pájaro" -> "🐦"
    "reptil" -> "🦎"
    else -> "🐾"
}

private fun estadoDescripcion(estado: EstadoMascotaUI): String = when (estado) {
    EstadoMascotaUI.PERDIDA -> "No sabes dónde está tu mascota"
    EstadoMascotaUI.ENCONTRADA -> "La encontraste o fue reportada"
    EstadoMascotaUI.NORMAL -> "Está contigo, todo bien"
    EstadoMascotaUI.CELO -> "Mascota en período de celo"
}

private fun estadoChipInfo(chip: String): Triple<String, String, String> = when (chip) {
    "PERDIDA" -> Triple("PERDIDA", "Perdidas", "🔴")
    "ENCONTRADA" -> Triple("ENCONTRADA", "Encontradas", "🟢")
    "NORMAL" -> Triple("NORMAL", "Normales", "🏠")
    "CELO" -> Triple("CELO", "En celo", "💛")
    else -> Triple("TODOS", "Todos", "🐾")
}

class MisMascotasViewModelFactory(
    private val tokenManager: TokenManager,
    private val mascotaApiService: MascotaApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MisMascotasViewModel(tokenManager, mascotaApiService) as T
}