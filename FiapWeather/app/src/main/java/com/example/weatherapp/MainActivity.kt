package com.example.weatherapp

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var CITY: String
    val API: String = "b91e39184f3b34721ba3a452caaa670e"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CITY = intent.getStringExtra("CITY_NAME") ?: ""

        weatherTask().execute()
        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            val intent = Intent(this, CityInputActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API&lang=pt_br").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)

                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Atualizado: "+ SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale("pt", "br")).format(Date(updatedAt*1000))
                val temp = main.getString("temp")+"°C"
                val tempMin = "Mínima: " + main.getString("temp_min")+"°C"
                val tempMax = "Máxima: " + main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                val nuvem = jsonObj.getJSONObject("clouds")
                val numeronuvem = nuvem.getString("all")

                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text =  updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.wind).text = "${windSpeed} m/s"
                findViewById<TextView>(R.id.pressure).text = "${numeronuvem}%"
                findViewById<TextView>(R.id.humidity).text = "${humidity}%"

                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                // Quando ocorrer um erro, vai para a tela de erro
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE

                // Inicie a atividade de erro
                val intent = Intent(this@MainActivity, ErrorActivity::class.java)
                startActivity(intent)
            }

        }
    }
}
