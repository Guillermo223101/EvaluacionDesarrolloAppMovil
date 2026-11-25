package com.example.evaluacion

import android.util.Log
import com.example.evaluacion.model.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test

import com.example.evaluacion.model.data.remote.SolicitudRemota
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

import org.junit.Assert.assertFalse

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

    @Test
    fun debeCrearSolicitudEnMongo() = runBlocking<Unit> {
        // 1 Preparar Solicitud
        val nuevaSolicitud = SolicitudRemota(
            titulo = "Prueba POST desde Android",
            descripcion = "Solicitud creada con Retrofit",
            estado = "Recibida",
            prioridad = "Media"
        )

        //2 Enviar POST a Spring Boot
        val guardada = RetrofitClient.api.crearSolicitud(nuevaSolicitud)

        //3 Comprobar respuesta
        assertTrue(!guardada.id.isNullOrBlank())
        assertEquals("Prueba POST desde Android", guardada.titulo)

        //4 Consultar nuevamente mediante GET
        val solicitudes = RetrofitClient.api.listarSolicitudes()

        //5 Verificar que aparezca la solicitud creada
        assertTrue(solicitudes.any { it.id == guardada.id })
        Log.d(
            "RETROFIT_POST",
            "Solicitud creada correctamente, ID: ${guardada.id}"
        )
    }

    @Test
    fun debeActualizarSolicitudEnMongo() = runBlocking<Unit> {
        // Obtener todas las solicitudes existentes
        val solicitudes = RetrofitClient.api.listarSolicitudes()

        // Buscar nuestra solicitud de prueba
        val solicitudOriginal = solicitudes.firstOrNull{
            it.titulo == "Prueba POST desde Android"
        } ?: throw AssertionError(
            "No se encontro la solicitud creada con POST"
        )

        // Recuperar su identificador
        val id = requireNotNull(solicitudOriginal.id)

        //Preparar los nuevos datos
        val datosActualizados = SolicitudRemota(
            titulo = "Prueba PUT desde Android",
            descripcion = "Solicitud actualizada con Retrofit",
            estado = "En proceso",
            prioridad = "Alta"
        )

        // Enviar PUT a Spring Boot
        val actualizada = RetrofitClient.api.actualizarSolicitud(id, datosActualizados)

        // Comprobar los resultados
        assertEquals(id, actualizada.id)
        assertEquals("Prueba PUT desde Android", actualizada.titulo)
        assertEquals("En proceso", actualizada.estado)
        assertEquals("Alta", actualizada.prioridad)

        // Comprobar la persistencia con GET
        val listaActualizada = RetrofitClient.api.listarSolicitudes()

        val encontrada = listaActualizada.firstOrNull{
            it.id == id
        }

        assertNotNull(encontrada)
        assertEquals("Prueba PUT desde Android",encontrada?.titulo)

        Log.d(
            "RETROFIT_PUT",
            "Solicitud actualizada correctamente. ID: $id"
        )
    }


    @Test
    fun debeEliminarSolicitudEnMongo() = runBlocking<Unit> {

        // 1. Consultar las solicitudes
        val solicitudes = RetrofitClient.api.listarSolicitudes()

        // 2. Buscar solamente nuestra solicitud de prueba
        val candidatas = solicitudes.filter {
            it.titulo == "Prueba PUT desde Android" &&
                    it.descripcion == "Solicitud actualizada con Retrofit"
        }

        // Debe existir exactamente una solicitud coincidente
        assertEquals(1, candidatas.size)

        // 3. Obtener el ID
        val id = requireNotNull(candidatas.single().id)

        // 4. Enviar DELETE a Spring Boot
        val respuesta = RetrofitClient.api.eliminarSolicitud(id)

        // 5. Verificar HTTP 204
        assertEquals(204, respuesta.code())

        // 6. Consultar nuevamente MongoDB
        val solicitudesRestantes = RetrofitClient.api.listarSolicitudes()

        // 7. Verificar que ya no existe
        assertFalse( solicitudesRestantes.any { it.id == id })

        Log.d(
            "RETROFIT_DELETE",
            "Solicitud eliminada correctamente. ID: $id"
        )
    }

}