package com.cletaeats.auth

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val usuarioId: Long? = null,
    val clienteId: Long? = null,
    val repartidorId: Long? = null,
    val nombre: String? = null,
    val correo: String? = null,
    val rol: String? = null,
    val estado: String? = null,
    val fotoUrl: String? = null
)