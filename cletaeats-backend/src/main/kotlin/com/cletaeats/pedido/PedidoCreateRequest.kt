package com.cletaeats.pedido

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.math.BigDecimal

data class PedidoCreateRequest(

    @field:NotBlank(message = "La dirección es obligatoria")
    val direccionEntrega: String,

    @field:DecimalMin(value = "0.10", message = "La distancia debe ser mayor a 0")
    val distanciaKm: BigDecimal,

    val observaciones: String? = null,

    @field:NotEmpty(message = "Debe incluir al menos un combo")
    val items: List<@Valid PedidoCreateItemRequest>,

    val medioPago: String?,
    val tarjetaResumen: String?
)