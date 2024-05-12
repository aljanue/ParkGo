package xabier.alberto.uv.es.parkgo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT:Long = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)

        Log.d("SplashActivity", "Esperando 2 segundos...")

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this,MainActivity::class.java))

            Log.d("SplashActivity", "Abriendo actividad principal")

            finish()
        }, SPLASH_TIME_OUT)
    }
}