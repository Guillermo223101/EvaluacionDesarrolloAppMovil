package com.example.evaluacion.utils

import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


// Protege las contraseñas utilizando PBKDF2.
// Cada contraseña recibe un salt aleatorio.
// La contraseña original nunca se guarda en Room.

object PasswordHash {

    private const val ITERACIONES = 150_000
    private const val BITS = 256

    // Seleccionar algoritmo compatible con Android
    private fun algoritmoActual(): String {
        return if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) {
            "PBKDF2WithHmacSHA256"
        } else {
            "PBKDF2WithHmacSHA1"
        }
    }

    // Generar el hash para almacenar en Room
    fun generar(clave: String): String {

        val algoritmo = algoritmoActual()

        val salt = ByteArray(16).also {
            SecureRandom().nextBytes(it)
        }

        val hash = derivar(
            clave = clave,
            salt = salt,
            iteraciones = ITERACIONES,
            algoritmo = algoritmo
        )

        return listOf(
            algoritmo,
            ITERACIONES.toString(),
            codificar(salt),
            codificar(hash)
        ).joinToString(":")
    }

    // Comprobar una contraseña durante el login
    fun verificar(
        clave: String,
        guardado: String
    ): Boolean {

        return try {

            val partes = guardado.split(":")

            if (
                partes.size != 4 ||
                partes[0] !in listOf(
                    "PBKDF2WithHmacSHA256",
                    "PBKDF2WithHmacSHA1"
                )
            ) {
                false
            } else {

                val iteraciones = partes[1].toInt()

                if (iteraciones !in 10_000..1_000_000) {
                    false
                } else {

                    val salt = Base64.decode(
                        partes[2],
                        Base64.NO_WRAP
                    )

                    val hashGuardado = Base64.decode(
                        partes[3],
                        Base64.NO_WRAP
                    )

                    if (
                        salt.size != 16 ||
                        hashGuardado.size != BITS / 8
                    ) {
                        false
                    } else {

                        val hashIngresado = derivar(
                            clave,
                            salt,
                            iteraciones,
                            partes[0]
                        )

                        MessageDigest.isEqual(
                            hashGuardado,
                            hashIngresado
                        )
                    }
                }
            }

        } catch (e: Exception) {
            false
        }
    }

    // Función interna de derivación PBKDF2
    private fun derivar(
        clave: String,
        salt: ByteArray,
        iteraciones: Int,
        algoritmo: String
    ): ByteArray {

        val spec = PBEKeySpec(
            clave.toCharArray(),
            salt,
            iteraciones,
            BITS
        )

        return try {

            SecretKeyFactory
                .getInstance(algoritmo)
                .generateSecret(spec)
                .encoded

        } finally {
            spec.clearPassword()
        }
    }

    // Convertir los bytes a texto Base64
    private fun codificar(
        bytes: ByteArray
    ): String {

        return Base64.encodeToString(
            bytes,
            Base64.NO_WRAP
        )
    }
}
