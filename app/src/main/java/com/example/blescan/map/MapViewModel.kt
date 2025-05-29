import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.LineString
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

const val MapTAG = "MapScreen"
val locationChannel = Channel<Pair<Double, Double>>()


const val minY = -860.1
const val maxY = 891.3
const val minX = -82.6
const val maxX = 834.46
const val ROUTE_SOURCE_ID = "routeSource"

object LocationData {
    var longitude: Float = 26.1025f // default - Unirii
    var latitude: Float = 44.4268f
    var zoom : Double = 16.0
}

class MapViewModel : ViewModel() {
    var routeLine by mutableStateOf<LineString?>(null)

    var totalDistance by mutableStateOf(0.0)

    var selectedMenu by mutableStateOf("HeatMap")
    var selectedIndex by mutableStateOf(0)

    var isChecked by mutableStateOf(true)
    var startedRoute by mutableStateOf(false)
    var startedHeatMap by mutableStateOf(true)

    val locationChannel = Channel<Pair<Double, Double>>()
    private val _locationState = MutableStateFlow(Pair(LocationData.longitude, LocationData.latitude))
    val locationState = _locationState.asStateFlow()

    fun updateRoute(newRoute: LineString?) {
        Log.d(MapTAG, "updateRoute" + routeLine.toString())
        routeLine =  newRoute
    }
    fun deleteRoute() {
        routeLine = null
    }
    fun printRoute()
    {
        Log.d(MapTAG, "printRoute" + routeLine.toString())
    }

    fun updateDistance(distance: Double) {
        totalDistance = distance
    }

    fun updateMenu(menu: String) {
        selectedMenu = menu
    }

    fun toggleRoute() {
        startedRoute = !startedRoute
    }

}