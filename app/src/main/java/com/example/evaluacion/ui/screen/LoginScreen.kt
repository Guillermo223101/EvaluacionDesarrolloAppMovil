package com.example.evaluacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.evaluacion.viewmodel.UsuarioViewModel

@Composable
fun LoginScreen(
    viewModel: UsuarioViewModel,
    onLoginExitoso: () -> Unit,
    onIrRegistro: () -> Unit
) {

    // Observar los datos del formulario
    val estado by viewModel.login.collectAsState()

    // Controlar si se muestra la contraseña
    var mostrarClave by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ingresa tu correo y contraseña",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CORREO ELECTRÓNICO
        OutlinedTextField(
            value = estado.correo,
            onValueChange = viewModel::onLoginCorreoChange,
            label = {
                Text("Correo electrónico")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            enabled = !estado.procesando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CONTRASEÑA
        OutlinedTextField(
            value = estado.clave,
            onValueChange = viewModel::onLoginClaveChange,
            label = {
                Text("Contraseña")
            },
            singleLine = true,
            visualTransformation = if (mostrarClave) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(
                    onClick = {
                        mostrarClave = !mostrarClave
                    }
                ) {
                    Text(
                        if (mostrarClave) "Ocultar" else "Ver"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            enabled = !estado.procesando,
            modifier = Modifier.fillMaxWidth()
        )

        // MENSAJE DE ERROR
        estado.error?.let { mensaje ->

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // BOTÓN INICIAR SESIÓN
        Button(
            onClick = {
                viewModel.iniciarSesion(
                    onExito = onLoginExitoso
                )
            },
            enabled = !estado.procesando,
            modifier = Modifier.fillMaxWidth()
        ) {

            if (estado.procesando) {

                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )

            } else {

                Text("Iniciar sesión")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BOTÓN PARA IR AL REGISTRO
        TextButton(
            onClick = onIrRegistro,
            enabled = !estado.procesando
        ) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }
    }
}
