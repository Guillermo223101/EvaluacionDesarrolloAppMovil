package com.example.evaluacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.evaluacion.model.UsuarioErrores
import com.example.evaluacion.model.UsuarioUiState
import com.example.evaluacion.model.data.local.UsuarioDatabase
import com.example.evaluacion.model.data.local.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.evaluacion.utils.PasswordHash
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val procesando: Boolean = false,
    val error: String? = null
)

data class UsuarioSesion(
    val id: Long,
    val nombre: String,
    val correo: String,
    val direccion: String
)
class UsuarioViewModel(app: Application): AndroidViewModel(app) {
    private val dao = UsuarioDatabase.getDatabase(app).usuarioDao()

    // Estado del registro
    private val _registroError = MutableStateFlow<String?>(null)
    val registroError: StateFlow<String?> = _registroError

    private val _registrando = MutableStateFlow(false)
    val registrando: StateFlow<Boolean> = _registrando

    // Estado del formulario de login
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    // Usuario autenticado durante la sesion actual
    private val _usuarioAutenticado = MutableStateFlow<UsuarioSesion?>(null)
    val usuarioAutenticado: StateFlow<UsuarioSesion?> = _usuarioAutenticado

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
        _registroError.value = null
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
        //Evitar registros simultaneos
        if (_registrando.value) return
        //Validar formulario
        if (!validarFormulario()) return

        val datos = _estado.value

        _registrando.value = true
        _registroError.value  = null

        viewModelScope.launch {
            try {
                // Normalizar el correo
                val correoNormalizado = datos.correo.trim().lowercase()

                // Comprobar si el correo existe
                val existente = withContext(Dispatchers.IO) {
                    dao.obtenerPorCorreo(correoNormalizado)
                }

                if (existente != null) {
                    _registroError.value =
                        "Este correo ya esta registrado"
                    return@launch
                }

                // Proteger la contrasena mediante PBKDF2
                val hash = withContext(Dispatchers.Default) {
                    PasswordHash.generar(datos.clave)
                }

                // Registrar usuario en Room
                withContext(Dispatchers.IO){
                    dao.insertar(
                        UsuarioEntity(
                            nombre = datos.nombre.trim(),
                            correo = correoNormalizado,
                            clave = hash,
                            direccion = datos.direccion.trim()
                        )
                    )
                }

                //Eliminar la contrasena del estado temporal
                _estado.update {
                    it.copy(clave = "")
                }

                //Preparar el correo para el futuro login
                _login.update {
                    it.copy(
                        correo = correoNormalizado,
                        clave = "",
                        error = null
                    )
                }

                //Confirmar registro exitoso
                onListo()
            } catch (e: CancellationException){
                throw e
            } catch (e: Exception){
                _registroError.value =
                    "No se pudo registrar el usuario"
            }finally {
                _registrando.value = false
            }
        }
    }

    //Actualizar correo del formulario de login
    fun onLoginCorreoChange(valor: String){
        _login.update {
            it.copy(
                correo = valor,
                error = null
            )
        }
    }

    // Actualizar contraseña del formulario
    fun onLoginClaveChange(valor: String) {
        _login.update {
            it.copy(
                clave = valor,
                error = null
            )
        }
    }

    // VALIDAR CREDENCIALES E INICIAR SESIÓN
    fun iniciarSesion(onExito: () -> Unit) {

        if (_login.value.procesando) return

        val datos = _login.value

        if (datos.correo.isBlank() || datos.clave.isBlank()) {
            _login.update {
                it.copy(
                    error = "Ingresa correo y contraseña"
                )
            }
            return
        }

        _login.update {
            it.copy(
                procesando = true,
                error = null
            )
        }

        viewModelScope.launch {

            try {
                // Buscar el usuario en Room
                val usuario = withContext(Dispatchers.IO) {
                    dao.obtenerPorCorreo(
                        datos.correo.trim().lowercase()
                    )
                }

                if (usuario == null || usuario.clave.isBlank()) {
                    _login.update {
                        it.copy(
                            error = "Correo o contraseña incorrectos"
                        )
                    }
                    return@launch
                }

                // Comprobar la contraseña ingresada
                val esCorrecta = withContext(Dispatchers.Default) {
                    PasswordHash.verificar(
                        clave = datos.clave,
                        guardado = usuario.clave
                    )
                }

                if (!esCorrecta) {
                    _login.update {
                        it.copy(
                            error = "Correo o contraseña incorrectos"
                        )
                    }
                    return@launch
                }

                // Crear la sesión sin exponer el hash
                _usuarioAutenticado.value = UsuarioSesion(
                    id = usuario.id,
                    nombre = usuario.nombre,
                    correo = usuario.correo,
                    direccion = usuario.direccion
                )

                // Limpiar contraseña temporal
                _login.update {
                    it.copy(
                        clave = "",
                        error = null
                    )
                }

                // Acceso autorizado
                onExito()

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _login.update {
                    it.copy(
                        error = "No se pudo iniciar sesión"
                    )
                }

            } finally {
                _login.update {
                    it.copy(procesando = false)
                }
            }
        }
    }
    // CERRAR SESIÓN
    fun cerrarSesion() {
        _usuarioAutenticado.value = null
        _login.value = LoginUiState()
        _estado.value = UsuarioUiState()
        _registroError.value = null
    }
}


