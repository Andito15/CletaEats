package com.cletaeats.combo

import java.math.BigDecimal

data class ComboResponse(
    val id: Long?,
    val restauranteId: Long?,
    val restauranteNombre: String?,
    val numeroCombo: Int,
    val nombre: String,
    val descripcion: String,
    val precio: BigDecimal,
    val estado: String,
    val imagenUrl: String?
)