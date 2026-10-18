package com.example.evaluacion.model

data class SolicitudUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val errores: SolicitudError = SolicitudError(),
    val guardando: Boolean = false,
    val errorGuardado: String? = null
)