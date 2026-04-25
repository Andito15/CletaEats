package com.cletaeats.calificacion

import java.time.LocalDateTime

data class CalificacionResponse(
    val calificacionId: Long?,
    val pedidoId: Long?,
    val repartidorId: Long?,
    val clienteId: Long?,
    val puntajeAmabilidad: Int,
    val puntajeTiempo: Int,
    val puntajePresentacion: Int,
    val comentario: String?,
    val fechaRegistro: LocalDateTime
)