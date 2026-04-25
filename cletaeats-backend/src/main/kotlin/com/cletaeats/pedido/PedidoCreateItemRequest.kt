package com.cletaeats.pedido

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class PedidoCreateItemRequest(
    @field:NotNull(message = "El comboId es obligatorio")
    val comboId: Long,

    @field:Min(value = 1, message = "La cantidad debe ser mayor o igual a 1")
    val cantidad: Int
)