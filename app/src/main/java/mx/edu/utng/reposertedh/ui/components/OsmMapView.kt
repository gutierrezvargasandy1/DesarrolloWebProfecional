package mx.edu.utng.reposertedh.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmMapView(
    latitud: Double,
    longitud: Double,
    titulo: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context: Context ->
            // Configurar OSMDroid
            Configuration.getInstance().userAgentValue = context.packageName

            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isClickable = true

                val punto = GeoPoint(latitud, longitud)

                // Zoom y centrar en la ubicación
                controller.setZoom(16.0)
                controller.setCenter(punto)

                // Marcador
                val marker = Marker(this)
                marker.position = punto
                marker.title = titulo
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                overlays.add(marker)
            }
        },
        update = { mapView ->
            val punto = GeoPoint(latitud, longitud)
            mapView.controller.setCenter(punto)
        }
    )
}