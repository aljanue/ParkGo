package xabier.alberto.uv.es.parkgo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class ParkActionReceiver : BroadcastReceiver() {
    private val fusedLocationClient: FusedLocationProviderClient = Singletons.fusedLocationProviderClient
    private val database: DatabaseReference = Singletons.database
    private lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        getLastLocation(::decreaseLocationCount)

        // Cancela la notificación
        with(NotificationManagerCompat.from(context)) {
            cancel(100)
        }

        // Inicia MainActivity
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            // Añade las banderas FLAG_ACTIVITY_NEW_TASK, FLAG_ACTIVITY_CLEAR_TASK y FLAG_ACTIVITY_BROUGHT_TO_FRONT
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        }
        context.startActivity(mainActivityIntent)
    }

    private fun getLastLocation(onLocationFetched: (String) -> Unit) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    if (location != null) {
                        val currentLocation = "${location.latitude},${location.longitude}"
                        Log.d("PeatonHome", "Got location: $currentLocation")
                        onLocationFetched(currentLocation)
                    } else {
                        Log.d("PeatonHome", "Location is null")
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun decreaseLocationCount(location: String) {
        val safeLocation = location.replace(".", "_")
        val locationRef = database.child(safeLocation)
        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val locationData = dataSnapshot.getValue(LocationData::class.java)
                if (locationData != null) {
                    // La ubicación existe, resta 1 a plazas
                    val newPlazas = (locationData.plazas ?: 0) - 1
                    if (newPlazas <= 0) {
                        // Si plazas es 0 o menos, borra la ubicación
                        locationRef.removeValue()
                        Toast.makeText(context, "Lugar marcado como OCUPADO", Toast.LENGTH_SHORT).show()
                    } else {
                        // Si no, actualiza el valor de plazas
                        locationRef.child("plazas").setValue(newPlazas)
                        Toast.makeText(context, "Lugar marcado como OCUPADO", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Aquí puedes manejar el error
            }
        })
    }
}