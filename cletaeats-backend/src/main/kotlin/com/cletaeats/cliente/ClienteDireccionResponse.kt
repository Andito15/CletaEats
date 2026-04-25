package com.cletaeats.cliente

data class ClienteDireccionResponse(
    val direccionId: Long?,
    val alias: String,
    val direccionTexto: String,
    val latitud: Double,
    val longitud: Double,
    val esPredeterminada: Boolean
)