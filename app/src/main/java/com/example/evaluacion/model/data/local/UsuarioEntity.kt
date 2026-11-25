package com.example.evaluacion.model.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.ColumnInfo
import com.example.evaluacion.model.RolUsuario
@Entity(tableName = "usuarios")
data class UsuarioEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,
    val correo: String,
    val clave: String,
    val direccion: String,

    //Rol inicial para usuarios registrados
    @ColumnInfo(defaultValue = "'CLIENTE'")
    val rol: String = RolUsuario.CLIENTE.name
)