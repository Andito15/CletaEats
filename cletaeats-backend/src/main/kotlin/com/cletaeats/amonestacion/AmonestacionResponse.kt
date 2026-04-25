package com.cletaeats.amonestacion

import java.time.LocalDateTime

data class AmonestacionResponse(
    val amonestacionId: Long?,
    val repartidorId: Long?,
    val quejaId: Long?,
    val adminUsuarioId: Long?,
    val motivo: String,
    val activa: String,
    val fechaAmonestacion: LocalDateTime
)