package mx.edu.utng.petfinder.ui.MascotaModule.PantallaMisMascotas

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import mx.edu.utng.petfinder.data.remote.MascotasModule.Dto.MascotaDto
import mx.edu.utng.petfinder.utils.ImageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisMascotasScreen(
    navController: NavController,
    viewModel: MascotasViewModel,
    onAgregarMascota: () -> Unit,
    onEditarMascota: (Int) -> Unit,
    onCrearReporte: (Int, String) -> Unit
) {
    val filteredMascotas by viewModel.filteredMascotas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<MascotaDto?>(null) }
    var showEstadoDialog by remember { mutableStateOf<MascotaDto?>(null) }

    val estados = listOf("TODOS", "ACTIVO", "PERDIDO", "ENCELO", "ADOPCION")

    // 🔥 IMPORTANTE: Recargar datos cada vez que la pantalla es visible
    LaunchedEffect(Unit) {
        Log.d("MASCOTAS_DEBUG", "Pantalla MisMascotas visible - Recargando datos")
        viewModel.cargarMascotas()
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            viewModel.resetDeleteSuccess()
            viewModel.cargarMascotas()
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Mascota") },
            text = { Text("¿Estás seguro de que quieres eliminar a ${showDeleteDialog?.nombre}?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog?.id_mascota?.let { viewModel.eliminarMascota(it) }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para cambiar estado
    if (showEstadoDialog != null) {
        val mascota = showEstadoDialog!!
        AlertDialog(
            onDismissRequest = { showEstadoDialog = null },
            title = { Text("Cambiar Estado") },
            text = {
                Column {
                    Text("Selecciona el nuevo estado para ${mascota.nombre}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    estadoOpciones.forEach { estado ->
                        TextButton(
                            onClick = {
                                showEstadoDialog = null
                                if (estado == "PERDIDO" || estado == "ENCELO" || estado == "ADOPCION") {
                                    onCrearReporte(mascota.id_mascota!!, estado)
                                } else {
                                    // Aquí iría la llamada para actualizar solo el estado de la mascota
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(estado)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEstadoDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Mascotas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    // 🔥 Botón de refresh manual
                    IconButton(onClick = { viewModel.cargarMascotas() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                    IconButton(onClick = onAgregarMascota) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar mascota")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Mostrar error si existe
            error?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Campo de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre...") },
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
                        label = { Text(estado) },
                        selected = selectedFilter == estado,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4CAF50),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de mascotas
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                }
                filteredMascotas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tienes mascotas registradas",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAgregarMascota,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Agregar mi primera mascota")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredMascotas) { mascota ->
                            MascotaCard(
                                mascota = mascota,
                                onVerDetalle = {
                                    navController.navigate("mascota_detalle/${mascota.id_mascota}")
                                },
                                onEditar = {
                                    onEditarMascota(mascota.id_mascota!!)
                                },
                                onEliminar = {
                                    showDeleteDialog = mascota
                                },
                                onCambiarEstado = {
                                    showEstadoDialog = mascota
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
fun MascotaCard(
    mascota: MascotaDto,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onCambiarEstado: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Foto
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    val fullImageUrl = ImageHelper.getFullImageUrl(mascota.foto_url)
                    if (!fullImageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = fullImageUrl,
                            contentDescription = "Foto de mascota",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            tint = Color.Gray
                        )
                    }
                }

                // Información
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mascota.nombre ?: "Sin nombre",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${mascota.especie} - ${mascota.raza ?: "Mestizo"}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Edad: ${mascota.edad ?: "No especificada"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Badge de estado
                Surface(
                    shape = CircleShape,
                    color = when (mascota.estado?.uppercase()) {
                        "ACTIVO" -> Color(0xFF4CAF50)
                        "PERDIDO" -> Color(0xFFFF9800)
                        "ENCELO" -> Color(0xFFE91E63)
                        "ADOPCION" -> Color(0xFF2196F3)
                        else -> Color.Gray
                    }
                ) {
                    Text(
                        text = mascota.estado ?: "ACTIVO",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF2196F3))
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }
                IconButton(onClick = onCambiarEstado) {
                    Icon(Icons.Default.Add, contentDescription = "Cambiar estado", tint = Color(0xFFFF9800))
                }
            }
        }
    }
}

private val estadoOpciones = listOf("ACTIVO", "PERDIDO", "ENCELO", "ADOPCION")