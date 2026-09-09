package com.davidchura.mapasodin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidchura.mapasodin.ui.theme.MapasOdinTheme
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity2 : ComponentActivity() {
    private lateinit var placesClient: PlacesClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Places.initializeWithNewPlacesApiEnabled(
            applicationContext, "AIzaSyAQeeBobsChLryCwdkxFCQdscMPpJw08H0"
        )
        placesClient = Places.createClient(this)
        setContent {
            MapasOdinTheme {
                DibujarMapa2(placesClient)
            }
        }
    }
}
@Composable
fun DibujarMapa2(placesClient: PlacesClient) {
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf(listOf<HashMap<String,String>>()) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var ubicacion by remember { mutableStateOf(
        LatLng(-12.06940257514952, -77.03407410521658))}

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacion, 18f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = ubicacion),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
            title = "Partenòn",
            snippet = "Templo Griego",
            draggable = true
        )
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 32.dp).padding(top = 32.dp)
                .background(color = Color.White),
            value = query,
            label = { Text("Buscar un lugar") },
            onValueChange = { newValue ->
                query = newValue

                if(query.length > 3) {

                    val zonaCircular: CircularBounds = CircularBounds
                        .newInstance(ubicacion, 2000.0)

                    val request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(query)
                        .setCountries("PE","CL","BO")
                        .setLocationRestriction(zonaCircular)
                        .setTypesFilter(listOf(PlaceTypes.RESTAURANT, PlaceTypes.CAFE))
                        .build()
                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            predictions = response.autocompletePredictions.map { prediction ->
                                Log.d("CONSULTA", prediction.getFullText(null).toString())
                                hashMapOf(
                                    "place_id" to prediction.placeId,
                                    "description" to prediction.getFullText(null).toString()
                                )
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("ERROR", exception.message.toString())
                            exception.printStackTrace()
                        }
                }
            }
        )
        if(predictions.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).background(color = Color.White)
            ) {
                predictions.forEach { prediction ->
                    Text(text = prediction["description"].toString(),
                        modifier = Modifier.padding(8.dp)
                            .clickable{
                                val placeId = prediction["place_id"].toString()
                                val placelocation = listOf(Place.Field.LOCATION)
                                val placeRequest = FetchPlaceRequest.newInstance(
                                    placeId, placelocation
                                )
                                placesClient.fetchPlace(placeRequest).addOnSuccessListener {
                                    selectedLocation = it.place.location
                                    ubicacion = selectedLocation!!
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                        selectedLocation!!, 18f
                                    )
                                    predictions = emptyList()
                                    query = prediction["description"].toString()
                                }
                            })
                }
            }
        }
    }
}