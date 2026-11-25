package com.example.evaluacion.model.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class RegistroRemotoRequest(
    val nombre: String,
    val correo: String,
    val clave: String,
    val direccion: String
)

data class UsuarioRegistradoRemoto(
    val id: String,
    val nombre: String,
    val correo: String,
    val direccion: String?,
    val rol: String
)

interface UsuarioApiService {
    @POST("api/usuarios/login")
    suspend fun iniciarSesion(
        @Body datos: LoginRemotoRequest
    ): LoginRemotoResponse

    @POST("api/usuarios/registro")
    suspend fun registrarUsuario(
        @Body datos: RegistroRemotoRequest
    ): UsuarioRegistradoRemoto
}