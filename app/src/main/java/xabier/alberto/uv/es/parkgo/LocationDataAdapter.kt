package xabier.alberto.uv.es.parkgo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.RecyclerView

class LocationDataAdapter(private var locations: List<LocationData>, private val searchedLocation: LocationData) :
    RecyclerView.Adapter<LocationDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val direccion: TextView = view.findViewById(R.id.direccion)
        val distancia: TextView = view.findViewById(R.id.distancia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("LocationDataAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plaza, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("LocationDataAdapter", "onBindViewHolder called for position $position")
        val location = locations[position]
        holder.direccion.text = location.direccion

        val distance = location.distance

        val formattedDistance = String.format("%.2f", distance)

        holder.distancia.text = "${formattedDistance} km"

        holder.itemView.setOnClickListener {
            // Crea y envía la notificación antes de abrir Google Maps
            Handler(Looper.getMainLooper()).postDelayed({
                // Crea y envía la notificación
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val name = "Notification Channel"
                    val descriptionText = "Channel for ParkGo reminder"
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val channel = NotificationChannel("ParkGoChannel", name, importance).apply {
                        description = descriptionText
                    }
                    val notificationManager: NotificationManager =
                        it.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }

                val parkIntent = Intent(it.context, ParkActionReceiver::class.java)
                val parkPendingIntent: PendingIntent = PendingIntent.getBroadcast(it.context, 0, parkIntent,
                    PendingIntent.FLAG_IMMUTABLE)

                val builder = NotificationCompat.Builder(it.context, "ParkGoChannel")
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("Park&Go")
                    .setContentText("Don't forget to mark 'Park' when you arrive!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .addAction(R.drawable.logo, "Aparcar", parkPendingIntent)

                with(NotificationManagerCompat.from(it.context)) {
                    if (ActivityCompat.checkSelfPermission(
                            it.context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            it.context as MainActivity,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            1
                        )
                    } else {
                        notify(100, builder.build())
                    }
                }
            }, 5000)

            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(location.direccion)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            it.context.startActivity(mapIntent)

            Handler(Looper.getMainLooper()).postDelayed({
                val enRutaIntent = Intent(it.context, EnRuta::class.java)
                it.context.startActivity(enRutaIntent)
            }, 3000)
        }
    }

    override fun getItemCount(): Int {
        Log.d("LocationDataAdapter", "getItemCount called, size: ${locations.size}")
        return locations.size
    }

    fun updateData(newLocations: List<LocationData>) {
        Log.d("LocationDataAdapter", "updateData called, new data size: ${newLocations.size}")
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