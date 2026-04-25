package com.cletaeats.pedido

import jakarta.validation.constraints.NotBlank

data class PedidoEstadoRequest(
    @field:NotBlank(message = "El estado es obligatorio")
    val estado: String
)