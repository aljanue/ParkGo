package xabier.alberto.uv.es.parkgo

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.net.URL

class GeocodingTask(private val onLocationFetched: (Pair<Double, Double>?) -> Unit) : AsyncTask<String, Void, Pair<Double, Double>?>() {
    override fun doInBackground(vararg params: String): Pair<Double, Double>? {
        val query = params[0]
        Log.d("GeocodingTask", "Buscando ubicación para $query")
        val apiKey = "ded8e751bb08410fbbcc5c7ae242ada1" // Your OpenCage API key
        val response = URL("https://api.opencagedata.com/geocode/v1/json?q=$query&key=$apiKey").readText()
        Log.d("GeocodingTask", "Respuesta: $response")
        val jsonObject = JSONObject(response)
        val results = jsonObject.getJSONArray("results")
        Log.d("GeocodingTask", "Resultados: ${results.length()}")
        if (results.length() > 0) {
            val result = results.getJSONObject(0)
            Log.d("GeocodingTask", "Resultado: $result")
            val geometry = result.getJSONObject("geometry")
            val lat = geometry.getDouble("lat")
            Log.d("GeocodingTask", "Latitud: $lat")
            val lon = geometry.getDouble("lng")
            Log.d("GeocodingTask", "Longitud: $lon")
            return Pair(lat, lon)
        }
        Log.d("GeocodingTask", "No se encontraron resultados")
        return null
    }

    override fun onPostExecute(result: Pair<Double, Double>?) {
        onLocationFetched(result)
    }
}