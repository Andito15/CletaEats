package com.cletaeats.combo

import jakarta.validation.constraints.NotBlank

data class ComboEstadoRequest(
    @field:NotBlank(message = "El estado es obligatorio")
    val estado: String
)