package com.cletaeats.queja

import java.time.LocalDateTime

data class QuejaResponse(
    val quejaId: Long?,
    val pedidoId: Long?,
    val repartidorId: Long?,
    val repartidorNombre: String?,
    val clienteId: Long?,
    val clienteNombre: String?,
    val categoria: String,
    val descripcion: String,
    val estado: String,
    val fechaRegistro: LocalDateTime
)