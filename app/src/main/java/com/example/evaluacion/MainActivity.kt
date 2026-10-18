package com.example.evaluacion

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import com.example.evaluacion.navigation.AppNavigation
import com.example.evaluacion.ui.theme.EvaluacionTheme
import com.example.evaluacion.ui.viewmodel.QrViewModel
import com.example.evaluacion.utils.CameraPermissionHelper

class MainActivity : ComponentActivity() {

    private val qrViewModel: QrViewModel by viewModels()

    private var hasCameraPermission by mutableStateOf(false)

    // Solicitar permiso de cámara
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            hasCameraPermission = isGranted

            if (isGranted) {
                Toast.makeText(
                    this,
                    "Permiso de cámara concedido",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Se necesita permiso de cámara para escanear QR",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasCameraPermission =
            CameraPermissionHelper.hasCameraPermission(this)

        enableEdgeToEdge()

        setContent {
            EvaluacionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(
                        qrViewModel = qrViewModel,
                        hasCameraPermission = hasCameraPermission,
                        onRequestCameraPermission = {
                            requestPermissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        hasCameraPermission =
            CameraPermissionHelper.hasCameraPermission(this)
    }
}
