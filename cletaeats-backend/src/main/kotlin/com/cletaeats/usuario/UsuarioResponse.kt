package com.cletaeats.usuario

data class UsuarioResponse(
    val id: Long?,
    val nombre: String,
    val correo: String,
    val cedula: String,
    val telefono: String,
    val estado: String,
    val rol: String
)