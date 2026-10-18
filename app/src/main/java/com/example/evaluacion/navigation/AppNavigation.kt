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
import com.example.evaluacion.ui.screen.CrearSolicitudScreen
import com.example.evaluacion.ui.screen.ListaSolicitudesScreen
import com.example.evaluacion.ui.view.QrScannerScreen
import com.example.evaluacion.ui.viewmodel.QrViewModel
import com.example.evaluacion.viewmodel.UsuarioViewModel
import com.example.evaluacion.viewmodel.SolicitudViewModel

@Composable
fun AppNavigation(
    qrViewModel: QrViewModel,
    hasCameraPermission: Boolean,
    onRequestCameraPermission: () -> Unit
) {

    // Controlador de navegación
    val navController = rememberNavController()

    // ViewModel de usuarios
    val usuarioViewModel: UsuarioViewModel = viewModel()

    // ViewModel de solicitudes
    val solicitudViewModel: SolicitudViewModel = viewModel()

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
                },

                onCrearSolicitud = {
                    navController.navigate("crear_solicitud")
                },

                onVerSolicitudes = {
                    navController.navigate("lista_solicitudes")
                }
            )
        }

        // Lector QR
        composable("qr") {
            QrScannerScreen(
                viewModel = qrViewModel,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = onRequestCameraPermission
            )
        }

        // Lista de solicitudes
        composable("lista_solicitudes") {
            ListaSolicitudesScreen(
                viewModel = solicitudViewModel,

                onNuevaSolicitud = {
                    navController.navigate("crear_solicitud")
                }
            )
        }

        // Formulario de solicitudes
        composable("crear_solicitud") {
            CrearSolicitudScreen(
                viewModel = solicitudViewModel,

                onGuardada = {
                    navController.navigate("lista_solicitudes") {
                        popUpTo("resumen") {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
