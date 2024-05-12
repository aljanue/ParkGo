package xabier.alberto.uv.es.parkgo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EnRuta : AppCompatActivity() {
    private val database: DatabaseReference = Singletons.database
    private val fusedLocationClient: FusedLocationProviderClient = Singletons.fusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.en_ruta)

        val aparcarButton: Button = findViewById(R.id.aparcar)
        aparcarButton.setOnClickListener {
            getLastLocation(::decreaseLocationCount)

            // Cancela la notificación
            with(NotificationManagerCompat.from(this)) {
                cancel(100)
            }

            val intent = Intent(this, MainActivity::class.java)
            // Iniciar MainActivity
            startActivity(intent)
        }

    }
    private fun getLastLocation(onLocationFetched: (String) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

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

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }
    private fun decreaseLocationCount(location: String) {
        val safeLocation = location.replace(".", "_")
        val locationRef = database.child(safeLocation)
        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val locationData = dataSnapshot.getValue(LocationData::class.java)
                if (locationData != null) {
                    val newPlazas = (locationData.plazas ?: 0) - 1
                    if (newPlazas <= 0) {
                        locationRef.removeValue()
                        Toast.makeText(applicationContext, "Lugar marcado como OCUPADO", Toast.LENGTH_SHORT).show()
                    } else {
                        locationRef.child("plazas").setValue(newPlazas)
                        Toast.makeText(applicationContext, "Lugar marcado como OCUPADO", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // No manejamos errores, pero sin esta función no compila el programa
            }
        })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Channel"
            val descriptionText = "Channel for ParkGo reminder"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("ParkGoChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val parkIntent = Intent(this, ParkActionReceiver::class.java)
        val parkPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, parkIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "ParkGoChannel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Park&Go")
            .setContentText("Don't forget to mark 'Park' when you arrive!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.logo, "Aparcar", parkPendingIntent)
            .addAction(R.drawable.logo, "Aparcar", parkPendingIntent)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@EnRuta,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@EnRuta,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            } else {
                notify(100, builder.build())
            }
        }
    }
}