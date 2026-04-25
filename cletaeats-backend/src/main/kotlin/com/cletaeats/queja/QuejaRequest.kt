package com.cletaeats.queja

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class QuejaRequest(

    @field:NotBlank(message = "La categoría es obligatoria")
    @field:Size(max = 50)
    val categoria: String,

    @field:NotBlank(message = "La descripción es obligatoria")
    @field:Size(max = 500)
    val descripcion: String
)