package com.cletaeats.amonestacion

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AmonestacionRequest(
    @field:NotBlank(message = "El motivo es obligatorio")
    @field:Size(max = 300)
    val motivo: String
)