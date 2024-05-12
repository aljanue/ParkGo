package xabier.alberto.uv.es.parkgo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlin.math.*


class CarHome : AppCompatActivity() {

    private val database: DatabaseReference = Singletons.database
    private lateinit var adapter: LocationDataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        adapter = LocationDataAdapter(emptyList(), LocationData())

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val searchView: SearchView = findViewById(R.id.search)
        searchView.setIconifiedByDefault(true)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                GeocodingTask { location ->
                    if (location != null) {
                        val (lat, lon) = location
                        Log.d("coordenadas", "Coordenadas buscadas: lat=$lat, lon=$lon")
                        database.addValueEventListener(object : ValueEventListener {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val locationDataList = mutableListOf<LocationData>()
                                for (dataSnapshotChild in dataSnapshot.children) {
                                    val locationData =
                                        dataSnapshotChild.getValue(LocationData::class.java)
                                    if (locationData != null) {
                                        Log.d(
                                            "coordenadas",
                                            "Coordenadas de la base de datos: ${locationData.coordenadas}"
                                        )
                                        val distance =
                                            calculateDistance(lat, lon, locationData.coordenadas)
                                        // Muestra los elementos a una distancia máxima de 3 km
                                        Log.d("coordenadas", "Distancia: $distance")
                                        if (distance <= 3) {
                                            locationData.distance = distance
                                            Log.d("coordenadas", "Añadiendo elemento a la lista $locationData.direccion")
                                            locationDataList.add(locationData)
                                        }
                                    }
                                }

                                locationDataList.sortBy { it.distance }

                                adapter.updateData(locationDataList)
                                adapter.notifyDataSetChanged()
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // No manejamos errores, pero sin esta función no compila el programa
                            }
                        })
                    }else{
                        Log.d("coordenadas", "No se encontraron coordenadas")
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.car_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_change_to_peaton_mode) {
            val intent = Intent(this, PeatonHome::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, coordinates: String?): Double {
        val (lat2, lon2) = coordinates?.split(",")?.map { it.toDouble() } ?: listOf(0.0, 0.0)
        val earthRadius = 6371.0 // radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}