package com.cletaeats.pedido

import java.math.BigDecimal

data class PedidoItemResponse(
    val comboId: Long?,
    val numeroCombo: Int,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: BigDecimal,
    val subtotalLinea: BigDecimal
)