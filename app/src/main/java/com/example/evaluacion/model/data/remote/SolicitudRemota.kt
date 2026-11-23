package com.example.evaluacion.model.data.remote

data class SolicitudRemota(
    val id: String? = null,
    val titulo: String,
    val descripcion: String,
    val estado: String,
    val prioridad: String
)