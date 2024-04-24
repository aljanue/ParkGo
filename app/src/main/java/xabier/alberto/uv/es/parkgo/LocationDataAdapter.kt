package xabier.alberto.uv.es.parkgo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationDataAdapter(private var locations: List<LocationData>, private val searchedLocation: LocationData) :
    RecyclerView.Adapter<LocationDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val direccion: TextView = view.findViewById(R.id.direccion)
        val distancia: TextView = view.findViewById(R.id.distancia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plaza, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = locations[position]
        holder.direccion.text = location.direccion

        // Asumiendo que tus coordenadas son una cadena en el formato "lat,lon"
        val (lat1, lon1) = location.coordenadas?.split(',')?.map { it.toDouble() } ?: listOf(0.0, 0.0)
        val (lat2, lon2) = searchedLocation.coordenadas?.split(',')?.map { it.toDouble() } ?: listOf(0.0, 0.0)
        val distance = calculateDistance(lat1, lon1, lat2, lon2)

        holder.distancia.text = "${distance}km"
    }

    override fun getItemCount() = locations.size

    fun updateData(newLocations: List<LocationData>) {
        this.locations = newLocations
        notifyDataSetChanged()
    }

    public fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // radio de la tierra en kilómetros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
}