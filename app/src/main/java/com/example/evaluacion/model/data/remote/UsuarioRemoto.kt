package com.example.evaluacion.model.data.remote

data class LoginRemotoRequest (
    val correo: String,
    val clave: String
)

data class UsuarioRemoto(
    val id: String,
    val nombre: String,
    val correo: String,
    val direccion: String?,
    val rol: String
)

data class LoginRemotoResponse(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: String,
    val usuario: UsuarioRemoto
)