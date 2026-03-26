package mx.edu.utng.reposertedh.ui.reporte

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

// ── Paleta (misma que MisMascotas) ────────────────────────────────────────────
private val PetOrange  = Color(0xFFFF6B35)
private val PetOrangeL = Color(0xFFFF8C5A)
private val PetYellow  = Color(0xFFFFD166)
private val PetCream   = Color(0xFFFFF8F0)
private val PetBrown   = Color(0xFF6B3F1F)
private val PetGreen   = Color(0xFF06D6A0)
private val PetRed     = Color(0xFFEF5350)
private val PetBlue    = Color(0xFF42A5F5)
private val SurfaceW   = Color(0xFFFFFFFF)
private val TxtPrimary = Color(0xFF2D1B0E)
private val TxtSecond  = Color(0xFF8B6347)
private val BgTop      = Color(0xFFFFEDD5)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoReporteScreen(
    viewModel: NuevoReporteViewModel,
    idMascota: Int? = null,          // pre-cargado si viene de "mascota perdida"
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val state   by viewModel.state.collectAsState()

    var descripcion       by remember { mutableStateOf("") }
    var latitud           by remember { mutableStateOf<Double?>(null) }
    var longitud          by remember { mutableStateOf<Double?>(null) }
    var direccionTexto    by remember { mutableStateOf("") }
    var obtenendoGps      by remember { mutableStateOf(false) }
    var gpsError          by remember { mutableStateOf<String?>(null) }
    var fotoUri           by remember { mutableStateOf<Uri?>(null) }
    var fotoFile          by remember { mutableStateOf<File?>(null) }
    var tempUri           by remember { mutableStateOf<Uri?>(null) }
    var tempFile          by remember { mutableStateOf<File?>(null) }
    val snackbarHost      = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        if (state is NuevoReporteState.Success) onSuccess()
        if (state is NuevoReporteState.Error)
            snackbarHost.showSnackbar("⚠️ ${(state as NuevoReporteState.Error).message}")
    }

    // ── Helpers de foto ───────────────────────────────────────────────────
    fun crearArchivo(): Pair<File, Uri> {
        val ts  = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val f   = File(context.cacheDir, "reporte_$ts.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", f)
        return f to uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { fotoUri = tempUri; fotoFile = tempFile }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            fotoUri = it
            try {
                val ins  = context.contentResolver.openInputStream(it)
                val file = File(context.cacheDir, "reporte_gal_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { o -> ins?.copyTo(o) }
                fotoFile = file
            } catch (_: Exception) {}
        }
    }
    val camPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) { val (f, u) = crearArchivo(); tempFile = f; tempUri = u; cameraLauncher.launch(u) }
    }
    fun abrirCamara() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val (f, u) = crearArchivo(); tempFile = f; tempUri = u; cameraLauncher.launch(u)
        } else camPermLauncher.launch(Manifest.permission.CAMERA)
    }

    // ── GPS ───────────────────────────────────────────────────────────────
    fun obtenerGps() {
        obtenendoGps = true; gpsError = null
        scope.launch {
            try {
                val fused = LocationServices.getFusedLocationProviderClient(context)
                val last: android.location.Location? = suspendCancellableCoroutine { cont ->
                    fused.lastLocation
                        .addOnSuccessListener { l -> if (cont.isActive) cont.resume(l, null) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null, null) }
                }
                val loc: android.location.Location? = if (last != null) last else {
                    suspendCancellableCoroutine { cont ->
                        val req = com.google.android.gms.location.CurrentLocationRequest.Builder()
                            .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                            .setDurationMillis(10_000).setMaxUpdateAgeMillis(0).build()
                        fused.getCurrentLocation(req, null)
                            .addOnSuccessListener { l: android.location.Location? -> if (cont.isActive) cont.resume(l, null) }
                            .addOnFailureListener { if (cont.isActive) cont.resume(null, null) }
                    }
                }
                if (loc != null) {
                    latitud = loc.latitude; longitud = loc.longitude
                    try {
                        val url  = "https://nominatim.openstreetmap.org/reverse?lat=${loc.latitude}&lon=${loc.longitude}&format=json"
                        val json = withContext(Dispatchers.IO) {
                            val c = URL(url).openConnection()
                            c.setRequestProperty("User-Agent", "MisMascotasApp/1.0")
                            c.connectTimeout = 6000; c.readTimeout = 6000
                            c.getInputStream().bufferedReader().readText()
                        }
                        direccionTexto = JSONObject(json).optString("display_name", "Ubicación actual")
                    } catch (_: Exception) {
                        direccionTexto = "%.5f, %.5f".format(loc.latitude, loc.longitude)
                    }
                } else gpsError = "No se pudo obtener la ubicación. Activa el GPS."
            } catch (e: Exception) { gpsError = "Error GPS: ${e.message}" }
            finally { obtenendoGps = false }
        }
    }

    val gpsPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true) obtenerGps()
        else gpsError = "Permiso de ubicación denegado"
    }

    fun pedirGps() {
        val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (ok) obtenerGps()
        else gpsPermLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    // Auto-obtener GPS al abrir
    LaunchedEffect(Unit) { pedirGps() }

    // ── UI ────────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = BgTop,
        snackbarHost = {
            SnackbarHost(snackbarHost) { data ->
                Snackbar(snackbarData = data, containerColor = PetBrown, contentColor = SurfaceW,
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(16.dp))
            }
        },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(BgTop, BgTop)))) {
                Row(modifier = Modifier.fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = PetBrown) }
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🔴", fontSize = 18.sp)
                            Text("Nuevo Reporte", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PetBrown)
                        }
                        Text("Publica un aviso para encontrar a tu mascota", fontSize = 11.sp, color = TxtSecond)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, Color(0xFFFFF3E0), PetCream)))) {

            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                drawCircle(PetOrange.copy(0.05f), 240.dp.toPx(), androidx.compose.ui.geometry.Offset(size.width * 0.9f, 0f))
                drawCircle(PetYellow.copy(0.07f), 160.dp.toPx(), androidx.compose.ui.geometry.Offset(0f, size.height * 0.9f))
            }

            Column(modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Tarjeta de mascota pre-cargada ──────────────────────
                if (idMascota != null) {
                    Surface(color = PetRed.copy(0.08f), shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PetRed.copy(0.3f)),
                        modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🐾", fontSize = 22.sp)
                            Column {
                                Text("Reporte vinculado a mascota #$idMascota", fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold, color = PetRed)
                                Text("Se asociará automáticamente al guardar", fontSize = 11.sp, color = TxtSecond)
                            }
                        }
                    }
                }

                // ── Sección GPS ─────────────────────────────────────────
                Card(shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceW),
                    elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.LocationOn, null, tint = PetOrange, modifier = Modifier.size(16.dp))
                                Text("Ubicación del avistamiento", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TxtPrimary)
                            }
                            Surface(onClick = { pedirGps() },
                                color = if (obtenendoGps) PetOrange.copy(0.2f) else PetOrange.copy(0.1f),
                                shape = RoundedCornerShape(10.dp)) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (obtenendoGps) CircularProgressIndicator(modifier = Modifier.size(12.dp), color = PetOrange, strokeWidth = 2.dp)
                                    else Icon(Icons.Default.MyLocation, null, tint = PetOrange, modifier = Modifier.size(14.dp))
                                    Text(if (obtenendoGps) "Buscando..." else "Actualizar GPS",
                                        fontSize = 11.sp, color = PetOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (gpsError != null) {
                            Surface(color = PetRed.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, tint = PetRed, modifier = Modifier.size(14.dp))
                                    Text(gpsError!!, fontSize = 11.sp, color = PetRed)
                                }
                            }
                        }

                        AnimatedVisibility(visible = latitud != null && longitud != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Chips lat/lon
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(color = PetOrange.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Outlined.PinDrop, null, tint = PetOrange, modifier = Modifier.size(11.dp))
                                            Text("%.5f".format(latitud), fontSize = 10.sp, color = PetBrown, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Surface(color = PetOrange.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Outlined.PinDrop, null, tint = PetOrange, modifier = Modifier.size(11.dp))
                                            Text("%.5f".format(longitud), fontSize = 10.sp, color = PetBrown, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                // Dirección reverse-geocoded
                                if (direccionTexto.isNotBlank()) {
                                    Surface(color = Color(0xFFF5EBE0), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Outlined.LocationOn, null, tint = TxtSecond, modifier = Modifier.size(13.dp).padding(top = 1.dp))
                                            Text(direccionTexto, fontSize = 11.sp, color = TxtSecond, lineHeight = 15.sp)
                                        }
                                    }
                                }
                                // Mini mapa OSM
                                latitud?.let { lat -> longitud?.let { lon ->
                                    key(lat, lon) {
                                        Box(modifier = Modifier.fillMaxWidth().height(160.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(2.dp, PetOrange.copy(0.3f), RoundedCornerShape(14.dp))) {
                                            AndroidView(modifier = Modifier.fillMaxSize(),
                                                factory = { ctx ->
                                                    Configuration.getInstance().userAgentValue = ctx.packageName
                                                    MapView(ctx).apply {
                                                        setTileSource(TileSourceFactory.MAPNIK)
                                                        setMultiTouchControls(false); isClickable = false
                                                        val p = GeoPoint(lat, lon)
                                                        controller.setZoom(16.0); controller.setCenter(p)
                                                        val m = Marker(this); m.position = p; m.title = "Avistamiento"
                                                        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); overlays.add(m)
                                                    }
                                                },
                                                update = { mv ->
                                                    val p = GeoPoint(lat, lon); mv.overlays.clear()
                                                    val m = Marker(mv); m.position = p; m.title = "Avistamiento"
                                                    m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                    mv.overlays.add(m); mv.controller.setZoom(16.0)
                                                    mv.controller.setCenter(p); mv.invalidate()
                                                })
                                            Surface(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                                                color = PetGreen.copy(0.92f), shape = RoundedCornerShape(7.dp)) {
                                                Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    Icon(Icons.Default.CheckCircle, null, tint = SurfaceW, modifier = Modifier.size(9.dp))
                                                    Text("GPS ok", fontSize = 9.sp, color = SurfaceW, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }}
                            }
                        }

                        if (latitud == null && !obtenendoGps) {
                            Surface(color = PetYellow.copy(0.25f), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Outlined.GpsNotFixed, null, tint = Color(0xFFB8860B), modifier = Modifier.size(16.dp))
                                    Text("Toca \"Actualizar GPS\" para obtener la ubicación", fontSize = 12.sp, color = Color(0xFFB8860B))
                                }
                            }
                        }
                    }
                }

                // ── Foto ────────────────────────────────────────────────
                Card(shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceW),
                    elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.PhotoCamera, null, tint = PetOrange, modifier = Modifier.size(16.dp))
                            Text("Foto (opcional)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TxtPrimary)
                        }
                        if (fotoUri != null) {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp)
                                .clip(RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFE0C8B0), RoundedCornerShape(12.dp))) {
                                androidx.compose.foundation.Image(
                                    painter = rememberAsyncImagePainter(fotoUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop)
                                // Botón cambiar foto
                                Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                    color = PetBrown.copy(0.85f), shape = RoundedCornerShape(8.dp),
                                    onClick = { abrirCamara() }) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.CameraAlt, null, tint = SurfaceW, modifier = Modifier.size(12.dp))
                                        Text("Cambiar", fontSize = 10.sp, color = SurfaceW, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { abrirCamara() }, modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, PetOrange.copy(0.5f))) {
                                    Icon(Icons.Default.CameraAlt, null, tint = PetOrange, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Cámara", color = PetOrange, fontSize = 13.sp)
                                }
                                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, PetBlue.copy(0.5f))) {
                                    Icon(Icons.Default.Photo, null, tint = PetBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Galería", color = PetBlue, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // ── Descripción ─────────────────────────────────────────
                Card(shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceW),
                    elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Description, null, tint = PetOrange, modifier = Modifier.size(16.dp))
                            Text("Descripción", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TxtPrimary)
                        }
                        OutlinedTextField(
                            value = descripcion, onValueChange = { descripcion = it },
                            placeholder = { Text("Describe dónde fue vista, características especiales...", fontSize = 12.sp, color = TxtSecond) },
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PetOrange, unfocusedBorderColor = Color(0xFFE0C8B0),
                                cursorColor = PetOrange, focusedContainerColor = Color(0xFFFFFAF5),
                                unfocusedContainerColor = Color(0xFFFFFAF5)
                            )
                        )
                    }
                }

                // ── Botón enviar ────────────────────────────────────────
                Button(
                    onClick = {
                        if (latitud != null && longitud != null) {
                            val archivoFinal = fotoFile?.let { viewModel.comprimirImagen(it) }

                            viewModel.crearReporte(
                                descripcion = descripcion,
                                lat  = latitud!!,
                                lng  = longitud!!,
                                idMascota   = idMascota,
                                imagenFile  = archivoFinal
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PetOrange,
                        disabledContainerColor = PetOrange.copy(0.4f)
                    ),
                    enabled = state !is NuevoReporteState.Loading && latitud != null
                ) {
                    if (state is NuevoReporteState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = SurfaceW
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Publicando...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    } else {
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Publicar reporte", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                if (latitud == null && state !is NuevoReporteState.Loading) {
                    Text("* Se requiere ubicación GPS para publicar el reporte",
                        fontSize = 11.sp, color = TxtSecond, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}