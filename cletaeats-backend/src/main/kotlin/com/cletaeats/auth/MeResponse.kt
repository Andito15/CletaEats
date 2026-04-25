package com.cletaeats.auth

data class MeResponse(
    val usuarioId: Long?,
    val nombre: String,
    val correo: String,
    val rol: String,
    val estado: String
)