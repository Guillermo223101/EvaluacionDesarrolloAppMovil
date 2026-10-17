package com.example.evaluacion.model.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios ORDER BY id DESC LIMIT 1")
    fun obtenerUltimo(): Flow<UsuarioEntity?>
}