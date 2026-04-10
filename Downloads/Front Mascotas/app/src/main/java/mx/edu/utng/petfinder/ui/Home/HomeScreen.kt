package mx.edu.utng.petfinder.ui.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import mx.edu.utng.petfinder.data.remote.ReportesModule.Dto.ReporteDto
import mx.edu.utng.petfinder.utils.ImageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {

    val filteredReportes by viewModel.filteredReportes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    val estados = listOf("TODOS", "PERDIDA", "ENCONTRADA", "CELO", "ADOPCION")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PetFinder",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Campo de búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.actualizarBusqueda(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar por nombre, especie, raza...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filtros horizontales
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(estados) { estado ->
                            FilterChip(
                                onClick = {
                                    viewModel.actualizarFiltro(if (selectedFilter == estado) null else estado)
                                },
                                label = {
                                    Text(
                                        when(estado) {
                                            "TODOS" -> "Todos"
                                            "PERDIDA" -> "Perdida"
                                            "ENCONTRADA" -> "Encontrada"
                                            "CELO" -> "Encelo"
                                            "ADOPCION" -> "Adopción"
                                            else -> estado
                                        }
                                    )
                                },
                                selected = selectedFilter == estado,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4CAF50),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de reportes
                    if (filteredReportes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No hay reportes que coincidan",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                                if (error != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.cargarReportes() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50)
                                        )
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredReportes) { reporte ->
                                ReporteCard(
                                    reporte = reporte,
                                    onClick = {
                                        navController.navigate("reporte_detalle/${reporte.id_reporte}")
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Menú lateral
            if (showMenu) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showMenu = false }
                ) {
                    Box(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "PetFinder",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Encuentra a tu mascota",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text("Mi Perfil") },
                                selected = false,
                                onClick = {
                                    showMenu = false
                                    navController.navigate("perfil")
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedTextColor = Color(0xFF4CAF50),
                                    selectedIconColor = Color(0xFF4CAF50)
                                )
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Mis Mascotas") },
                                selected = false,
                                onClick = {
                                    showMenu = false
                                    navController.navigate("mis_mascotas")
                                }
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Mis Reportes") },
                                selected = false,
                                onClick = {
                                    showMenu = false
                                    navController.navigate("mis_reportes")
                                }
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Mis Conversaciones") },
                                selected = false,
                                onClick = {
                                    showMenu = false
                                    navController.navigate("conversaciones")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReporteCard(
    reporte: ReporteDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto de la mascota
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                val fullImageUrl = ImageHelper.getFullImageUrl(reporte.mascota.foto_url)

                if (!fullImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = "Foto de mascota",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        tint = Color.Gray
                    )
                }
            }

            // Información del reporte
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = reporte.mascota.nombre ?: "Sin nombre",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    // Badge de estado
                    Surface(
                        shape = CircleShape,
                        color = when(reporte.estado.uppercase()) {
                            "PERDIDA" -> Color(0xFFFF9800)
                            "ENCONTRADA" -> Color(0xFF4CAF50)
                            "CELO" -> Color(0xFFE91E63)
                            "ADOPCION" -> Color(0xFF2196F3)
                            else -> Color.Gray
                        }
                    ) {
                        Text(
                            text = when(reporte.estado.uppercase()) {
                                "PERDIDA" -> "Perdida"
                                "ENCONTRADA" -> "Encontrada"
                                "CELO" -> "Encelo"
                                "ADOPCION" -> "Adopción"
                                else -> reporte.estado
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${reporte.mascota.especie} - ${reporte.mascota.raza ?: "Mestizo"}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                reporte.direccion?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = reporte.fecha_reporte?.take(10) ?: "Fecha no disponible",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}