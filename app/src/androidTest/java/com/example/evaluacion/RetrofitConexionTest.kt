package com.example.evaluacion

import android.util.Log
import com.example.evaluacion.model.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test

class RetrofitConexionTest {

    @Test
    fun debeObtenerSolcitudesDesdeSpringBoot() = runBlocking {
        // Ejecutamos una consulta GET con Retrofit
        val solicitudes = RetrofitClient.api.listarSolicitudes()

        // Mostramos la cantidad recibida
        Log.d(
            "RETROFIT_TEST",
            "Solicitudes recibidas: ${solicitudes.size}"
        )

        // Comprobamos que se obtuvo una lista
        assertNotNull(solicitudes)
    }
}