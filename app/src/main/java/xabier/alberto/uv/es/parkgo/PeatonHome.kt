package xabier.alberto.uv.es.parkgo

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.location.Geocoder
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class PeatonHome : AppCompatActivity() {
    private val database: DatabaseReference = Singletons.database
    private val fusedLocationClient: FusedLocationProviderClient = Singletons.fusedLocationProviderClient
    private val last_location: Location? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.peaton_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val libreButton: Button = findViewById(R.id.aparcar)
        libreButton.setOnClickListener {
            getLastLocation(::checkAndUpdateLocation)
        }

        val ocupadoButton: Button = findViewById(R.id.ocupado)
        ocupadoButton.setOnClickListener {
            getLastLocation(::decreaseLocationCount)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.person_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_change_to_peaton_mode) {
            val intent = Intent(this, CarHome::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
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
    private fun checkAndUpdateLocation(location: String) {
        val safeLocation = location.replace(".", "_")
        val locationRef = database.child(safeLocation)
        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val locationData = dataSnapshot.getValue(LocationData::class.java)
                if (locationData == null) {
                    // La ubicación no existe, añade la ubicación
                    val (latitude, longitude) = location.split(",").map { it.toDouble() }
                    val direccion = getAddressFromLocation(latitude, longitude)
                    val newLocationData = LocationData(direccion, location, "estado", 1)
                    locationRef.setValue(newLocationData)
                    Toast.makeText(applicationContext, "Ubicación añadida a la base de datos", Toast.LENGTH_SHORT).show()
                } else {
                    // La ubicación existe, suma 1 a plazas
                    val newPlazas = (locationData.plazas ?: 0) + 1
                    locationRef.child("plazas").setValue(newPlazas)
                    Toast.makeText(applicationContext, "Ubicación actualizada en la base de datos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Aquí puedes manejar el error
            }
        })
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
                        Toast.makeText(applicationContext, "Ubicación eliminada de la base de datos", Toast.LENGTH_SHORT).show()
                    } else {
                        // Si no, actualiza el valor de plazas
                        locationRef.child("plazas").setValue(newPlazas)
                        Toast.makeText(applicationContext, "Ubicación actualizada en la base de datos", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Aquí puedes manejar el error
            }
        })
    }
    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            return address.getAddressLine(0) // Returns the full address
        } else {
            return "Address not found"
        }
    }


}