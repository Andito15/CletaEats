package com.cletaeats.amonestacion

import java.time.LocalDateTime

data class AmonestacionAdminResponse(
    val amonestacionId: Long?,
    val quejaId: Long?,
    val repartidorId: Long?,
    val repartidorNombre: String?,
    val adminUsuarioId: Long?,
    val adminNombre: String?,
    val motivo: String,
    val activa: String,
    val fechaAmonestacion: LocalDateTime
)