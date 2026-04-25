package com.cletaeats.cliente

data class ClienteDireccionRequest(
    val alias: String,
    val direccionTexto: String,
    val latitud: Double?,
    val longitud: Double?,
    val esPredeterminada: Boolean?
)