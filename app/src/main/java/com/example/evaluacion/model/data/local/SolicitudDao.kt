
package com.example.evaluacion.model.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO encargado de las operaciones con solicitudes
@Dao
interface SolicitudDao {

    // Insertar una nueva solicitud en la base de datos
    @Insert
    suspend fun insertar(solicitud: SolicitudEntity)

    // Obtener todas las solicitudes, ordenadas
    // desde la más reciente hasta la más antigua
    @Query("SELECT * FROM solicitudes ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>
}
