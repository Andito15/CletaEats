package com.cletaeats.pedido

import java.math.BigDecimal
import java.time.LocalDateTime

data class PedidoResponse(
    val pedidoId: Long?,
    val numeroPedido: String,
    val estado: String,
    val fechaPedido: LocalDateTime,
    val fechaEntrega: LocalDateTime?,
    val clienteId: Long?,
    val clienteNombre: String?,
    val restauranteId: Long?,
    val restauranteNombre: String?,
    val repartidorId: Long?,
    val repartidorNombre: String?,
    val direccionEntrega: String,
    val distanciaKm: BigDecimal,
    val tipoTarifaDia: String,
    val costoKmAplicado: BigDecimal,
    val observaciones: String?,
    val items: List<PedidoItemResponse>,
    val factura: FacturaResumenResponse?,
    val repartidorFotoUrl: String? = null
)