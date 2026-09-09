package com.davidchura.mapasodin

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.davidchura.mapasodin.ui.theme.MapasOdinTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        geolocaliza()

        enableEdgeToEdge()

    }
    fun geolocaliza(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            solicitarPermisos()
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                setContent {
                    //val ubicacion = LatLng(-12.067114003136147, -77.03370304775581)
                    val ubicacion = LatLng(location!!.latitude, location!!.longitude)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(ubicacion, 18f)
                    }
                    MapasOdinTheme {
                        DibujarMapa(ubicacion, cameraPositionState)
                    }
                }
            }
    }
    fun solicitarPermisos(){
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
                permission ->
                when{
                    permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        geolocaliza()
                    }
                    permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        geolocaliza()
                    }
                }
        }
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

}

@Composable
fun DibujarMapa(ubicacion: LatLng, cameraPositionState: CameraPositionState) {
    val context = LocalContext.current

    val mapProperties by remember {
        mutableStateOf(MapProperties(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        ))
    }
    GoogleMap (
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties
    ){
        Marker(
            state = MarkerState(position = ubicacion),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
            title = "Partenòn",
            snippet = "Templo Griego",
            draggable = true
        )
        val ubicacionAntena = LatLng(-12.06940257514952, -77.03407410521658)
        Circle(
            center = ubicacionAntena,
            radius = 500.0,
            fillColor = Color(0x33FF0000),
            strokeColor = Color(0x99FF0000),
            clickable = true,
            onClick = {
                Toast.makeText(context, "Red 5G de 500 metros",
                    Toast.LENGTH_SHORT).show()
            }
        )

        val coordenadasPoligono = listOf(
            LatLng(-12.041472202437124, -77.03412168941529),
            LatLng(-12.048772877633096, -77.03906804316422),
            LatLng(-12.054485705933677, -77.03025257241426),
            LatLng(-12.043590547341344, -77.02455535537696),
            LatLng(-12.043471904394451, -77.02907689892288)
        )

        Polygon(
            points = coordenadasPoligono,
            fillColor = Color(0x33FFFF00),
            strokeColor = Color(0x99FFFF00),
            clickable = true,
            onClick = {
                Toast.makeText(context, "Zona intangible",
                    Toast.LENGTH_SHORT).show()
            }
        )
        val coordenadasPolilinea = listOf(
            LatLng(-12.065291347315165, -77.033584125885),
            LatLng(-12.063739077161557, -77.03412332541014),
            LatLng(-12.064459030231026, -77.0391934847436),
            LatLng(-12.060651192123505, -77.04125917659762)
        )

        Polyline(
            points = coordenadasPolilinea,
            width = 10f,
            color =  Color(0x990000CC),

            )
    }
}