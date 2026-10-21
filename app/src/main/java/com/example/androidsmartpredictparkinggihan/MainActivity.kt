package com.example.androidsmartpredictparkinggihan

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.androidsmartpredictparkinggihan.ui.navigation.SmartParkingNavHost
import com.example.androidsmartpredictparkinggihan.ui.theme.AndroidSmartPredictParkingGihanTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val app = application as SmartParkingApp
        enableEdgeToEdge()
        setContent {
            AndroidSmartPredictParkingGihanTheme {
                SmartParkingNavHost(app = app)
            }
        }
    }
}
