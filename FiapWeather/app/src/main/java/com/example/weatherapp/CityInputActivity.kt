package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import android.location.Geocoder
import android.util.Log
import java.io.IOException
import java.util.Locale


class CityInputActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_input)

        val editTextCity = findViewById<EditText>(R.id.editTextCity)
        val buttonGetWeather = findViewById<Button>(R.id.buttonGetWeather)
        val buttonUseLocation = findViewById<Button>(R.id.button) // Botão de "Usar minha localização"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        buttonGetWeather.setOnClickListener {
            val city = editTextCity.text.toString().trim()
            if (city.isNotEmpty()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("CITY_NAME", city)
                startActivity(intent)
            } else {
                val errorIntent = Intent(this, ErrorActivity::class.java)
                startActivity(errorIntent)
            }
        }

        buttonUseLocation.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permissão já concedida, pegar a localização
                getLastLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Mostrar uma explicação para o usuário
                Toast.makeText(this, "Permissão de localização é necessária para usar esta funcionalidade.", Toast.LENGTH_LONG).show()
                // Solicitar novamente a permissão
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                // Solicitar a permissão sem precisar de explicação
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getLastLocation()
        } else {
            Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation.addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val adminArea = address.adminArea ?: "Estado não encontrado"
                            // Verifica se o bairro está disponível
                            if (adminArea != "Estado não encontrado") {
                                val intent = Intent(this@CityInputActivity, MainActivity::class.java)
                                intent.putExtra("CITY_NAME", adminArea)
                                startActivity(intent)
                            } else {
                                navigateToErrorScreen()
                            }
                        } else {
                            Toast.makeText(this, "Endereço não encontrado para as coordenadas.", Toast.LENGTH_LONG).show()
                            navigateToErrorScreen()
                        }
                    } else {
                        Toast.makeText(this, "Localização está nula.", Toast.LENGTH_LONG).show()
                        navigateToErrorScreen()
                    }
                } else {
                    Toast.makeText(this, "Não foi possível obter a localização. Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    navigateToErrorScreen()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permissão de localização não está disponível.", Toast.LENGTH_LONG).show()
            navigateToErrorScreen()
        }
    }


    private fun navigateToErrorScreen() {
        val errorIntent = Intent(this@CityInputActivity, ErrorActivity::class.java)
        startActivity(errorIntent)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

}
