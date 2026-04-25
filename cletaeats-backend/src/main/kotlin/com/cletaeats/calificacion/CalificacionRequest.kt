package com.cletaeats.calificacion

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class CalificacionRequest(

    @field:Min(1)
    @field:Max(5)
    val puntajeAmabilidad: Int,

    @field:Min(1)
    @field:Max(5)
    val puntajeTiempo: Int,

    @field:Min(1)
    @field:Max(5)
    val puntajePresentacion: Int,

    @field:Size(max = 500)
    val comentario: String? = null
)