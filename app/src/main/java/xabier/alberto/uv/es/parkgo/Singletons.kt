package xabier.alberto.uv.es.parkgo

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.database.DatabaseReference

object Singletons {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var database: DatabaseReference
}