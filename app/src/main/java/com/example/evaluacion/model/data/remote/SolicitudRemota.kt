package com.example.evaluacion.model.data.remote

data class SolicitudRemota(
    val id: String? = null,
    val titulo: String,
    val descipcion: String,
    val estado: String,
    val prioridad: String
)