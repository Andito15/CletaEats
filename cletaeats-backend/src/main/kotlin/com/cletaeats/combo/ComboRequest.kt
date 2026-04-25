package com.cletaeats.combo

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ComboRequest(

    @field:Min(value = 1, message = "El número de combo debe ser entre 1 y 9")
    @field:Max(value = 9, message = "El número de combo debe ser entre 1 y 9")
    val numeroCombo: Int,

    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(max = 120)
    val nombre: String,

    @field:NotBlank(message = "La descripción es obligatoria")
    @field:Size(max = 300)
    val descripcion: String,

    @field:NotNull(message = "El precio es obligatorio")
    @field:DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    val precio: BigDecimal,

    @field:Size(max = 500)
    val imagenUrl: String? = null
)