package xabier.alberto.uv.es.parkgo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT:Long = 2000 // 2 sec
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)

        Log.d("SplashActivity", "Esperando 3 segundos...")

        Handler(Looper.getMainLooper()).postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity
            startActivity(Intent(this,MainActivity::class.java))

            Log.d("SplashActivity", "Abriendo actividad principal")

            // close this activity
            finish()
        }, SPLASH_TIME_OUT)
    }
}