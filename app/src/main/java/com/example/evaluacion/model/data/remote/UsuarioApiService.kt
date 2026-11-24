package com.example.evaluacion.model.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface UsuarioApiService {
    @POST("api/usuarios/login")
    suspend fun iniciarSesion(
        @Body datos: LoginRemotoRequest
    ): LoginRemotoResponse
}