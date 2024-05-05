package xabier.alberto.uv.es.parkgo

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Inicializa los objetos en Singletons
        Singletons.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        Singletons.database = FirebaseDatabase.getInstance("https://parkgo-c57ec-default-rtdb.europe-west1.firebasedatabase.app").getReference("ParkGo")

        val pedestrianImage: ImageView = findViewById(R.id.peaton)
        pedestrianImage.setOnClickListener {
            val intent = Intent(this, PeatonHome::class.java)
            startActivity(intent)
        }
        val carImage: ImageView = findViewById(R.id.car)
        carImage.setOnClickListener {
            val intent = Intent(this, CarHome::class.java)
            startActivity(intent)
        }
    }
}