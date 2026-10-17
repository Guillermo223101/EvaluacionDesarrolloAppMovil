package com.example.evaluacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.evaluacion.ui.theme.EvaluacionTheme
import com.example.evaluacion.ui.theme.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContent {
            EvaluacionTheme {
                HomeScreen()
            }
        }
    }
}