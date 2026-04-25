package com.cletaeats.pedido

import java.math.BigDecimal

data class FacturaResumenResponse(
    val numeroFactura: String,
    val subtotal: BigDecimal,
    val costoTransporte: BigDecimal,
    val porcentajeIva: BigDecimal,
    val montoIva: BigDecimal,
    val montoTotal: BigDecimal,
    val estadoPago: String,
    val medioPago: String
)