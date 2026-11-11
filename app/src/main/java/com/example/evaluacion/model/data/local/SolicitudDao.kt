
package com.example.evaluacion.model.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// DAO encargado de las operaciones con solicitudes
@Dao
interface SolicitudDao {

    // Insertar una nueva solicitud en la base de datos
    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    // Obtener todas las solicitudes, ordenadas
    // desde la más reciente hasta la más antigua
    @Query("SELECT * FROM solicitudes ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>

    // Obtener una solocitud especifica mediante su ID
    @Query("SELECT * FROM solicitudes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): SolicitudEntity?

    // UPDATE
    @Update
    suspend fun actualizar(solicitud: SolicitudEntity): Int

    // DELETE
    @Delete
    suspend fun eliminar(solicitud: SolicitudEntity): Int
}
