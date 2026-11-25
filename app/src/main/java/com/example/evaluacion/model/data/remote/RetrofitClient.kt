package com.example.evaluacion.model.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    //Direccion de Spring Boot desde el emulador Android
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //Servicio que utilizaremos para las peticiones
    val api: SolicitudApiService by lazy {
        retrofit.create(SolicitudApiService::class.java)
    }

    // Servicio para autenticacion remota
    val usuarios: UsuarioApiService by lazy {
        retrofit.create(UsuarioApiService::class.java)
    }

    //Servicio de solicitudes protegidas por JWT
    val solicitudesCliente: SolicitudClienteApiService by lazy {
        retrofit.create(SolicitudClienteApiService::class.java)
    }

    // Servicio para operaciones administrativas protegidas
    val solicitudesAdmin: SolicitudAdminApiService by lazy {
        retrofit.create(SolicitudAdminApiService::class.java)
    }

    // Operaciones exclusivas del tecnico
    val solicitudesTecnico: SolicitudTecnicoApiService by lazy {
        retrofit.create(SolicitudTecnicoApiService::class.java)
    }

}