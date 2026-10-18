package com.example.evaluacion.model.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val estado: String = "Recibida",
    val fechaCreacion: Long = System.currentTimeMillis()
)