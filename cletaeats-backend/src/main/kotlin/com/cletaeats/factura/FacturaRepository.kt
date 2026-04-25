package com.cletaeats.factura

import org.springframework.data.jpa.repository.JpaRepository

interface FacturaRepository : JpaRepository<FacturaEntity, Long> {
    fun findByPedido_PedidoId(pedidoId: Long): FacturaEntity?
}