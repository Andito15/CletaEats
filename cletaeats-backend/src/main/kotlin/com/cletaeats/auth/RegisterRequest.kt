package com.cletaeats.auth

data class RegisterRequest(
    val rol: String,
    val nombre: String,
    val cedula: String,
    val correo: String,
    val telefono: String,
    val password: String
)