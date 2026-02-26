package mx.edu.utng.reposertedh.ui.reporte

import android.Manifest
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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarReporteScreen(
    viewModel: EditarReporteViewModel,
    reporteId: Int,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // ── Estados del Formulario ──────────────────────────────────────────
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    // ── Foto State (Lógica de Cámara) ───────────────────────────────────
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var fotoFile by remember { mutableStateOf<File?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var tempFile by remember { mutableStateOf<File?>(null) }

    // Función para crear archivo temporal para la cámara
    fun crearArchivoTemporal(): Pair<File, Uri> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val archivo = File(context.cacheDir, "edit_foto_$timestamp.jpg")
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
            Log.d("CameraEdit", "Captura cancelada o fallida")
        }
    }

    // Launcher para pedir permiso de cámara
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

    // ── Ciclo de Vida ───────────────────────────────────────────────────

    // Cargar datos iniciales del reporte al iniciar
    LaunchedEffect(reporteId) {
        viewModel.cargarReporte(reporteId)
    }

    // Observar cambios de estado para navegación o llenado de datos
    LaunchedEffect(state) {
        when (state) {
            is EditarState.Success -> onSuccess()
            is EditarState.Loaded -> {
                val r = (state as EditarState.Loaded).reporte
                titulo = r.titulo
                descripcion = r.descripcion
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Reporte", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
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
            // ── Sección de Foto (Preview) ───────────────────────────────────
            if (fotoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = fotoUri),
                    contentDescription = "Nueva foto",
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
                    Text("📷 Cambiar foto capturada")
                }
            } else {
                OutlinedButton(
                    onClick = { abrirCamara() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(Modifier.width(8.dp))
                    Text("📷 Tomar nueva foto (Opcional)")
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Campos de Texto ─────────────────────────────────────────────
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(32.dp))

            // ── Mensaje de Error ───────────────────────────────────────────
            if (state is EditarState.Error) {
                Text(
                    text = (state as EditarState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ── Botón de Acción ────────────────────────────────────────────
            Button(
                onClick = {
                    // Ahora pasamos el 'context' para que el ViewModel pueda comprimir
                    viewModel.actualizar(context, reporteId, titulo, descripcion, fotoFile)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = state !is EditarState.Loading && titulo.isNotBlank() && descripcion.isNotBlank()
            ) {
                if (state is EditarState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar Cambios", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}