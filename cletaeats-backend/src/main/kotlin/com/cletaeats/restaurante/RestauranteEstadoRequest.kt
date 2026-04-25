package com.cletaeats.restaurante

import jakarta.validation.constraints.NotBlank

data class RestauranteEstadoRequest(
    @field:NotBlank(message = "El estado es obligatorio")
    val estado: String
)