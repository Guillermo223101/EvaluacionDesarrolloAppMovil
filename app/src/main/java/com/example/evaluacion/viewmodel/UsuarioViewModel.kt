package com.example.evaluacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evaluacion.model.UsuarioErrores
import com.example.evaluacion.model.UsuarioUiState
import com.example.evaluacion.model.data.local.UsuarioDatabase
import com.example.evaluacion.model.data.local.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsuarioViewModel(app: Application): AndroidViewModel(app) {
    private val dao = UsuarioDatabase.getDatabase(app).usuarioDao()

    // Estado interno mutable
    private val _estado = MutableStateFlow(UsuarioUiState())

    // Estado expuesto para la UI
    val estado: StateFlow<UsuarioUiState> = _estado

    // Actualiza el campo nombre y limpia su error
    fun onNombreChange(valor: String) {
        _estado.update {
            it.copy(
                nombre = valor,
                errores = it.errores.copy(nombre = null)
            )
        }
    }

    // Actualiza el campo correo
    fun onCorreoChange(valor: String) {
        _estado.update {
            it.copy(
                correo = valor,
                errores = it.errores.copy(correo = null)
            )
        }
    }

    // Actualiza el campo clave
    fun onClaveChange(valor: String) {
        _estado.update {
            it.copy(
                clave = valor,
                errores = it.errores.copy(clave = null)
            )
        }
    }

    // Actualiza el campo dirección
    fun onDireccionChange(valor: String) {
        _estado.update {
            it.copy(
                direccion = valor,
                errores = it.errores.copy(direccion = null)
            )
        }
    }

    // Actualiza checkbox de aceptación
    fun onAceptarTerminosChange(valor: Boolean) {
        _estado.update {
            it.copy(
                    aceptaTerminos = valor,
                    errores = it.errores.copy(terminos = null)
                )
        }
    }

    // Validación global del formulario
    fun validarFormulario(): Boolean {
        val estadoActual = _estado.value

        val errores = UsuarioErrores(
            nombre = if (estadoActual.nombre.isBlank())
                "Campo obligatorio"
            else null,

            correo = if (!estadoActual.correo.contains("@"))
                "Correo inválido"
            else null,

            clave = if (estadoActual.clave.length < 6)
                "Debe tener al menos 6 caracteres"
            else null,

            direccion = if (estadoActual.direccion.isBlank())
                "Campo obligatorio"
            else null,

            terminos = if (!estadoActual.aceptaTerminos)
                "Debes aceptar los terminos y condiciones"
            else null
        )

        val hayErrores = listOfNotNull(
            errores.nombre,
            errores.correo,
            errores.clave,
            errores.direccion,
            errores.terminos
        ).isNotEmpty()

        _estado.update {
            it.copy(errores = errores)
        }

        return !hayErrores
    }

    //GUARDAR EN ROOM
    fun guardar(onListo: () -> Unit) {
        val a = _estado.value
        viewModelScope.launch {
            dao.insertar(
                UsuarioEntity(
                    nombre = a.nombre,
                    correo = a.correo,
                    clave = "",
                    direccion = a.direccion
                )
            )
            // Limpiar la contraseña del estado temporal
            _estado.update {
                it.copy(clave = "")
            }

            onListo()
        }
    }
}


