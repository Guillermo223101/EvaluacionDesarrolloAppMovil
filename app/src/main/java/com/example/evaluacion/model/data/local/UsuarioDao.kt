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

    @Query(
        """
            SELECT * FROM usuarios
            WHERE LOWER(TRIM(correo)) = LOWER(TRIM(:correo))
            ORDER BY id DESC
            LIMIT 1
        """
    )

    suspend fun  obtenerPorCorreo(
        correo: String
    ): UsuarioEntity?
}