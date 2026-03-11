package mx.edu.utng.reposertedh.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import mx.edu.utng.reposertedh.data.TokenManager
import mx.edu.utng.reposertedh.model.ReporteResponse

// ── Paleta PetFinder ───────────────────────────────────────────────────────────
private val PetOrange      = Color(0xFFFF6B35)
private val PetOrangeLight = Color(0xFFFF8C5A)
private val PetYellow      = Color(0xFFFFD166)
private val PetCream       = Color(0xFFFFF8F0)
private val PetBrown       = Color(0xFF6B3F1F)
private val PetBrownLight  = Color(0xFF9B6B47)
private val PetGreen       = Color(0xFF06D6A0)
private val PetRed         = Color(0xFFEF5350)
private val PetBlue        = Color(0xFF42A5F5)
private val SurfaceWhite   = Color(0xFFFFFFFF)
private val TextPrimary    = Color(0xFF2D1B0E)
private val TextSecondary  = Color(0xFF8B6347)
private val BgGradientTop  = Color(0xFFFFEDD5)

// ── Tabs de sección ────────────────────────────────────────────────────────────
private enum class PetTab(val label: String, val emoji: String, val color: Color) {
    TODOS("Todos", "🐾", PetOrange),
    PERDIDOS("Perdidos", "🔴", PetRed),
    ENCONTRADOS("Encontrados", "✅", PetGreen)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onReporteClick: (Int) -> Unit,
    onNuevoReporte: () -> Unit,
    onMisReportes: () -> Unit,
    onChats: () -> Unit,
    onMisMascotas: () -> Unit,
    onLogout: () -> Unit
) {
    var query              by remember { mutableStateOf("") }
    var filtroEstado       by remember { mutableStateOf("TODOS") }
    var tabSeleccionado    by remember { mutableStateOf(PetTab.TODOS) }
    val state              by viewModel.state.collectAsState()
    val drawerState        = rememberDrawerState(DrawerValue.Closed)
    val scope              = rememberCoroutineScope()
    val context            = LocalContext.current
    val tokenManager       = remember { TokenManager(context) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PetDrawer(
                onMisReportes = { scope.launch { drawerState.close() }; onMisReportes() },
                onChats       = { scope.launch { drawerState.close() }; onChats() },
                onMisMascotas = { scope.launch { drawerState.close() }; onMisMascotas() },
                onLogout      = {
                    scope.launch {
                        tokenManager.clearToken()
                        drawerState.close()
                        onLogout()
                    }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = BgGradientTop,
            topBar = {
                PetTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    query       = query,
                    onQueryChange = {
                        query = it
                        viewModel.buscar(it)
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNuevoReporte,
                    containerColor = PetOrange,
                    contentColor = SurfaceWhite,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(12.dp, CircleShape)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp))
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BgGradientTop, Color(0xFFFFF3E0), PetCream)
                        )
                    )
            ) {
                // Círculo decorativo fondo
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = PetOrange.copy(alpha = 0.05f),
                        radius = 280.dp.toPx(),
                        center = Offset(size.width * 0.9f, 0f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // ── Tabs Perdidos / Encontrados / Todos ────────────────
                    PetTabRow(
                        selected = tabSeleccionado,
                        onSelect = { tab ->
                            tabSeleccionado = tab
                            when (tab) {
                                PetTab.TODOS        -> viewModel.cargarReportes()
                                PetTab.PERDIDOS     -> viewModel.filtrarPorEstado("PERDIDO")
                                PetTab.ENCONTRADOS  -> viewModel.filtrarPorEstado("ENCONTRADO")
                            }
                        }
                    )

                    // ── Chips de estado ────────────────────────────────────
                    EstadoChipsRow(
                        filtroSeleccionado = filtroEstado,
                        onFiltroChange = { filtro ->
                            filtroEstado = filtro
                            when (filtro) {
                                "TODOS"      -> viewModel.cargarReportes()
                                "RECIENTES"  -> viewModel.filtrarRecientes()
                                else         -> viewModel.filtrarPorEstado(filtro)
                            }
                        }
                    )

                    // ── Contador de resultados ─────────────────────────────
                    if (state is DashboardState.Success) {
                        val count = (state as DashboardState.Success).reportes.size
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("${tabSeleccionado.emoji}", fontSize = 14.sp)
                            Text(
                                "$count mascotas encontradas",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // ── Lista ──────────────────────────────────────────────
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (state) {
                            is DashboardState.Loading -> {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(color = PetOrange)
                                    Text("Buscando mascotas...", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                            is DashboardState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("😿", fontSize = 48.sp)
                                    Text(
                                        "Ocurrió un error",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        (state as DashboardState.Error).message,
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            is DashboardState.Success -> {
                                val reportes = (state as DashboardState.Success).reportes
                                if (reportes.isEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("🐾", fontSize = 52.sp)
                                        Text(
                                            "Sin reportes por aquí",
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "Intenta con otro filtro o sé el primero en reportar.",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 8.dp,
                                            bottom = 88.dp
                                        ),
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
    }
}

// ── Top Bar ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetTopBar(
    onMenuClick: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(colors = listOf(BgGradientTop, BgGradientTop))
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = PetBrown)
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("🐾", fontSize = 22.sp)
                Text(
                    "PetFinder",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PetBrown,
                    letterSpacing = (-0.5).sp
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = PetOrange.copy(alpha = 0.12f)
            ) {
                Text(
                    "🔴 EN VIVO",
                    fontSize = 10.sp,
                    color = PetOrange,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Barra de búsqueda
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Buscar mascota por nombre, raza...", fontSize = 13.sp, color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PetOrange) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
    }
}

// ── Tabs Perdidos / Encontrados ────────────────────────────────────────────────
@Composable
private fun PetTabRow(
    selected: PetTab,
    onSelect: (PetTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PetTab.values().forEach { tab ->
            val isSelected = selected == tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(tab) }
                    .shadow(if (isSelected) 6.dp else 0.dp, RoundedCornerShape(14.dp)),
                color = if (isSelected) tab.color else SurfaceWhite,
                shape = RoundedCornerShape(14.dp),
                border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE0C8B0)) else null
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(tab.emoji, fontSize = 18.sp)
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) SurfaceWhite else TextSecondary
                    )
                }
            }
        }
    }
}

// ── Chips de estado ────────────────────────────────────────────────────────────
@Composable
private fun EstadoChipsRow(
    filtroSeleccionado: String,
    onFiltroChange: (String) -> Unit
) {
    val chips = listOf("TODOS", "RECIENTES", "ENVIADO", "EN PROCESO", "RESUELTO")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        items(chips) { chip ->
            val isSelected = filtroSeleccionado == chip
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onFiltroChange(chip) },
                color = if (isSelected) PetYellow else SurfaceWhite,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, if (isSelected) PetYellow else Color(0xFFE0C8B0))
            ) {
                Text(
                    chip,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) PetBrown else TextSecondary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

// ── Card de reporte ────────────────────────────────────────────────────────────
@Composable
fun ReporteItemCard(reporte: ReporteResponse, onClick: () -> Unit) {
    val context      = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var token        by remember { mutableStateOf<String?>(null) }
    val baseUrl      = "http://100.89.122.81:8085"

    LaunchedEffect(Unit) { token = tokenManager.getToken() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Imagen superior full-width si existe
            if (!reporte.imagenUrl.isNullOrBlank() && token != null) {
                val request = ImageRequest.Builder(context)
                    .data("$baseUrl${reporte.imagenUrl}")
                    .setHeader("Authorization", "Bearer $token")
                    .crossfade(true)
                    .build()

                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(request),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder decorativo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFEDD5), Color(0xFFFFD8B1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            reporte.titulo.contains("perro", ignoreCase = true) ||
                                    reporte.titulo.contains("can", ignoreCase = true)   -> "🐕"
                            reporte.titulo.contains("gato", ignoreCase = true) ||
                                    reporte.titulo.contains("gata", ignoreCase = true)  -> "🐈"
                            else                                                 -> "🐾"
                        },
                        fontSize = 40.sp
                    )
                }
            }

            // Contenido
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = reporte.titulo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    EstadoBadge(reporte.estado)
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = reporte.descripcion,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(12.dp), tint = TextSecondary)
                        Text(
                            reporte.fechaReporte.take(10),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PetOrange.copy(alpha = 0.10f)
                    ) {
                        Text(
                            "Ver detalles →",
                            fontSize = 11.sp,
                            color = PetOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Badge de estado ────────────────────────────────────────────────────────────
@Composable
fun EstadoBadge(estado: String) {
    val (bgColor, textColor, label, emoji) = when (estado.uppercase()) {
        "ENVIADO"     -> listOf(PetBlue.copy(0.15f),  PetBlue,    "Enviado",    "📤")
        "EN PROCESO"  -> listOf(PetYellow.copy(0.25f), Color(0xFFB8860B), "En proceso", "⏳")
        "RESUELTO"    -> listOf(PetGreen.copy(0.15f),  Color(0xFF065F46), "Resuelto",  "✅")
        "PERDIDO"     -> listOf(PetRed.copy(0.12f),    PetRed,     "Perdido",    "🔴")
        "ENCONTRADO"  -> listOf(PetGreen.copy(0.15f),  Color(0xFF065F46), "Encontrado","🟢")
        else          -> listOf(Color(0xFFEEEEEE),     TextSecondary, estado,    "•")
    }

    Surface(
        color = bgColor as Color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "$emoji ${(label as String).uppercase()}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor as Color
        )
    }
}

// ── Drawer lateral ─────────────────────────────────────────────────────────────
@Composable
private fun PetDrawer(
    onMisReportes: () -> Unit,
    onChats: () -> Unit,
    onMisMascotas: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = PetCream,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFEDD5), PetCream)
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(8.dp, CircleShape)
                        .background(
                            Brush.radialGradient(colors = listOf(PetOrangeLight, PetOrange)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐾", fontSize = 30.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "PetFinder",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PetBrown
                )
                Text(
                    "Reencuentra a tu mejor amigo",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE0C8B0))

        Spacer(Modifier.height(8.dp))

        // Ítem: Mis Reportes
        DrawerItem(
            emoji = "📋",
            label = "Mis Reportes",
            sublabel = "Ver reportes que creaste",
            color = PetOrange,
            onClick = onMisReportes
        )

        // Ítem: Chats
        DrawerItem(
            emoji = "💬",
            label = "Chats",
            sublabel = "Mensajes con otros usuarios",
            color = PetBlue,
            onClick = onChats
        )

        // Ítem: Mis Mascotas
        DrawerItem(
            emoji = "🐾",
            label = "Mis Mascotas",
            sublabel = "Gestiona tus mascotas registradas",
            color = PetGreen,
            onClick = onMisMascotas
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = Color(0xFFE0C8B0)
        )

        // Estadística decorativa
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = PetGreen.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("✅", fontSize = 22.sp)
                Column {
                    Text("+12,400", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF065F46))
                    Text("mascotas reunidas", fontSize = 11.sp, color = Color(0xFF065F46))
                }
            }
        }

        Spacer(Modifier.weight(1f))

        HorizontalDivider(color = Color(0xFFE0C8B0))

        // Ítem: Cerrar sesión
        NavigationDrawerItem(
            icon = {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = PetRed)
            },
            label = {
                Text("Cerrar sesión", color = PetRed, fontWeight = FontWeight.SemiBold)
            },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = PetRed.copy(alpha = 0.07f)
            )
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerItem(
    emoji: String,
    label: String,
    sublabel: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            Column {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(sublabel, fontSize = 11.sp, color = TextSecondary)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = color, modifier = Modifier.size(18.dp))
        }
    }
}