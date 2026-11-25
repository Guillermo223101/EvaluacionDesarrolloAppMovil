package com.example.evaluacion.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.evaluacion.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun HomeScreen(){
    Scaffold(
        topBar = {
            // BARRA SUPERIOR CON TITULO
            TopAppBar(
                title = { Text("Mi App Kotlin") }
            )
        }//FIN TOPBAR
    ) { innerPadding ->
        // ORGANIZA LOS ELEMENTOS VERTICALMENTE
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .background(Color(0xFFF0F0F0)),
            // SEPARACION VERTICAL ENTRE ELEMENTOS
            verticalArrangement = Arrangement.spacedBy(20.dp),
            // CENTRA LOS ELEMENTOS HORIZONTALMENTE
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TEXTO DE BIENVENIDA USANDO MATERIALTHEME
            Text(
                text = "Bienvenido!",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall
            )//FIN DEL TEXT
            // BOTON
            Button(onClick = {/*accion futura*/ }) { Text("Presioname") }
            Image(
                // MUESTRA EL LOGO GUARDADO EN DRAWABLE
                painter = painterResource(
                    id = R.drawable.logo
                ),
                contentDescription = "Logo App",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Fit
            )
            // ORGANIZA TEXTOS HORIZONTALMENTE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Solicitudes")
                Text("Seguimientos")
            }
        } //FIN COLUMN
    }// FIN SCAFFOLD
}// FIN HOMESCREEN

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    HomeScreen()
}