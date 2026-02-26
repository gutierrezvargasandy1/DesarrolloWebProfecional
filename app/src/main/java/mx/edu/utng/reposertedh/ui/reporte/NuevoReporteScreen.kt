package mx.edu.utng.reposertedh.ui.reporte

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoReporteScreen(
    viewModel: NuevoReporteViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var ubicacionObtenida by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    // ── Foto State ────────────────────────────────────────────────────────
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var fotoFile by remember { mutableStateOf<File?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var tempFile by remember { mutableStateOf<File?>(null) }

    // Crea un archivo temporal
    fun crearArchivoTemporal(): Pair<File, Uri> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val archivo = File(context.cacheDir, "foto_reporte_$timestamp.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            archivo
        )
        return Pair(archivo, uri)
    }

    // Launcher para tomar la foto
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Solo si tuvo éxito, pasamos lo temporal al estado oficial
            fotoUri = tempUri
            fotoFile = tempFile
        } else {
            Log.d("Camera", "Captura cancelada o fallida")
        }
    }

    // Launcher para pedir permiso
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (file, uri) = crearArchivoTemporal()
            tempFile = file
            tempUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun abrirCamara() {
        val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            val (file, uri) = crearArchivoTemporal()
            tempFile = file
            tempUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    // ─────────────────────────────────────────────────────────────────────

    LaunchedEffect(state) {
        if (state is NuevoReporteState.Success) onSuccess()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { latitud = it.latitude; longitud = it.longitude; ubicacionObtenida = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { latitud = it.latitude; longitud = it.longitude; ubicacionObtenida = true }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo reporte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (ubicacionObtenida) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "📍 Ubicación obtenida: ${"%.4f".format(latitud)}, ${"%.4f".format(longitud)}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "⏳ Obteniendo ubicación GPS...",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Sección foto (Preview fija) ────────────────────────────────
            if (fotoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = fotoUri),
                    contentDescription = "Foto del reporte",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { abrirCamara() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📷 Volver a tomar foto")
                }
            } else {
                OutlinedButton(
                    onClick = { abrirCamara() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📷 Tomar foto (opcional)")
                }
            }
            // ─────────────────────────────────────────────────────────────

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del reporte") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(Modifier.height(24.dp))

            if (state is NuevoReporteState.Error) {
                Text(
                    (state as NuevoReporteState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    if (latitud != null && longitud != null) {
                        val archivoFinal = fotoFile?.let { viewModel.comprimirImagen(context, it) }
                        viewModel.crearReporte(titulo, descripcion, latitud!!, longitud!!, archivoFinal)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is NuevoReporteState.Loading && ubicacionObtenida
            ) {
                if (state is NuevoReporteState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enviar reporte")
                }
            }
        }
    }
}