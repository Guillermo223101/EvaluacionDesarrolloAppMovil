package com.example.evaluacion.navigation

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.evaluacion.ui.screen.RegistroScreen
import com.example.evaluacion.ui.screen.ResumenScreen
import com.example.evaluacion.ui.view.QrScannerScreen
import com.example.evaluacion.ui.viewmodel.QrViewModel
import com.example.evaluacion.viewmodel.UsuarioViewModel

@Composable
fun AppNavigation(
    qrViewModel: QrViewModel,
    hasCameraPermission: Boolean,
    onRequestCameraPermission: () -> Unit
) {

    val navController = rememberNavController()

    // ViewModel compartido entre Registro y Resumen
    val usuarioViewModel: UsuarioViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "registro",
        modifier = Modifier.safeDrawingPadding()
    ) {

        // Pantalla de registro
        composable("registro") {
            RegistroScreen(
                navController = navController,
                viewModel = usuarioViewModel
            )
        }

        // Pantalla de resumen
        composable("resumen") {
            ResumenScreen(
                viewModel = usuarioViewModel,
                onOpenQr = {
                    navController.navigate("qr")
                }
            )
        }

        // Pantalla de lector QR
        composable("qr") {
            QrScannerScreen(
                viewModel = qrViewModel,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = onRequestCameraPermission
            )
        }
    }
}
