package com.cletaeats.restaurante

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RestauranteRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(max = 120)
    val nombre: String,

    @field:NotBlank(message = "La cédula jurídica es obligatoria")
    @field:Size(max = 30)
    val cedulaJuridica: String,

    @field:NotBlank(message = "La dirección es obligatoria")
    @field:Size(max = 250)
    val direccion: String,

    @field:NotBlank(message = "El tipo de comida es obligatorio")
    @field:Size(max = 50)
    val tipoComida: String,

    @field:Size(max = 500)
    val imagenUrl: String? = null,

    val latitud: Double?,
    val longitud: Double?
)