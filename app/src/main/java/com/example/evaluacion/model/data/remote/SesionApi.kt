package com.example.evaluacion.model.data.remote

object SesionApi {
    @Volatile
    var token: String? = null
        private set

    fun guardarToken(nuevoToken: String){
        require(nuevoToken.isNotBlank())
        token = nuevoToken
    }

    fun cerrarSesion(){
        token = null
    }
}