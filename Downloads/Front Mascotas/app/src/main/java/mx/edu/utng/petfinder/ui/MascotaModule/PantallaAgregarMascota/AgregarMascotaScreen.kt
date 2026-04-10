package mx.edu.utng.petfinder.ui.MascotaModule.PantallaAgregarMascota

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AgregarMascotaScreen(
    navController: NavController,
    viewModel: AgregarMascotaViewModel
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val currentAddress by viewModel.currentAddress.collectAsState()

    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("ACTIVO") }
    var descripcion by remember { mutableStateOf("") }
    var direccionManual by remember { mutableStateOf("") }
    var usarUbicacionActual by remember { mutableStateOf(true) }

    // Estado de la foto
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }

    // Permisos de ubicación
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Launcher para seleccionar imagen de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            val file = File(context.cacheDir, "temp_image.jpg")
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            selectedImageFile = file
        }
    }

    // Launcher para tomar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageFile?.let { file ->
                selectedImageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            }
        }
    }

    // Crear archivo temporal para la foto
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.cacheDir
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    // Opciones para especies
    val especies = listOf("Perro", "Gato", "Hamster", "Conejo", "Ave", "Otro")
    val sexos = listOf("Macho", "Hembra")
    val estadosLista = listOf("ACTIVO", "PERDIDO", "ENCELO", "ADOPCION")

    // Navegar al éxito
    LaunchedEffect(success) {
        if (success) {
            viewModel.resetSuccess()
            navController.popBackStack()
        }
    }

    // Obtener ubicación al inicio
    LaunchedEffect(Unit) {
        if (locationPermissionState.status.isGranted) {
            viewModel.obtenerUbicacionActual()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Agregar Mascota",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selección de foto
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Foto de la mascota", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5))
                            .clickable {
                                // Mostrar diálogo de opciones
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Foto mascota",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AddCircle,
                                    contentDescription = "Agregar foto",
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.Gray
                                )
                                Text("Agregar foto", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Galería")
                        }
                        Button(
                            onClick = {
                                if (cameraPermissionState.status.isGranted) {
                                    val file = createImageFile()
                                    selectedImageFile = file
                                    cameraLauncher.launch(
                                        FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            file
                                        )
                                    )
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cámara")
                        }
                    }
                }
            }

            // Datos básicos
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Datos de la mascota", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Especie
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = {}
                    ) {
                        OutlinedTextField(
                            value = especie,
                            onValueChange = { especie = it },
                            label = { Text("Especie *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
                        )
                        DropdownMenu(
                            expanded = false,
                            onDismissRequest = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            especies.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = { especie = opcion }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = raza,
                        onValueChange = { raza = it },
                        label = { Text("Raza") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Sexo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sexos.forEach { opcion ->
                            FilterChip(
                                onClick = { sexo = opcion },
                                label = { Text(opcion) },
                                selected = sexo == opcion,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = edad,
                        onValueChange = { edad = it },
                        label = { Text("Edad") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Estado
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = {}
                    ) {
                        OutlinedTextField(
                            value = estado,
                            onValueChange = { estado = it },
                            label = { Text("Estado") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
                        )
                        DropdownMenu(
                            expanded = false,
                            onDismissRequest = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            estadosLista.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = { estado = opcion }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )
                }
            }

            // Ubicación
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Ubicación del hogar", fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { usarUbicacionActual = true },
                            label = { Text("Usar ubicación actual") },
                            selected = usarUbicacionActual,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            onClick = { usarUbicacionActual = false },
                            label = { Text("Ingresar manualmente") },
                            selected = !usarUbicacionActual,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (usarUbicacionActual) {
                        if (locationPermissionState.status.isGranted) {
                            Button(
                                onClick = { viewModel.obtenerUbicacionActual() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Obtener ubicación actual")
                            }

                            if (currentAddress.isNotEmpty()) {
                                OutlinedTextField(
                                    value = currentAddress,
                                    onValueChange = {},
                                    label = { Text("Dirección") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    readOnly = true
                                )
                            }

                            currentLocation?.let { (lat, lng) ->
                                Text(
                                    text = "Lat: ${"%.6f".format(lat)}, Lng: ${"%.6f".format(lng)}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Button(
                                onClick = { locationPermissionState.launchPermissionRequest() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Conceder permiso de ubicación")
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = direccionManual,
                            onValueChange = { direccionManual = it },
                            label = { Text("Dirección") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = { viewModel.buscarDireccion(direccionManual) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = direccionManual.isNotBlank()
                        ) {
                            Text("Buscar coordenadas")
                        }
                    }
                }
            }

            // Error
            error?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Botón guardar
            Button(
                onClick = {
                    if (especie.isNotBlank()) {
                        val (lat, lng) = if (usarUbicacionActual && currentLocation != null) {
                            Pair(currentLocation!!.first, currentLocation!!.second)
                        } else if (!usarUbicacionActual && currentLocation != null) {
                            Pair(currentLocation!!.first, currentLocation!!.second)
                        } else {
                            Pair(null, null)
                        }

                        viewModel.crearMascota(
                            nombre = nombre.ifBlank { null },
                            especie = especie,
                            raza = raza.ifBlank { null },
                            color = color.ifBlank { null },
                            sexo = sexo.ifBlank { null },
                            edad = edad.ifBlank { null },
                            estado = estado,
                            descripcion = descripcion.ifBlank { null },
                            direccion = if (usarUbicacionActual) currentAddress else direccionManual.ifBlank { null },
                            latitud = lat,
                            longitud = lng,
                            fotoFile = selectedImageFile
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = !isLoading && especie.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Guardar mascota")
                }
            }
        }
    }
}