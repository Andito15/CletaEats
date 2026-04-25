package com.cletaeats.pedido

import org.springframework.data.jpa.repository.JpaRepository

interface PedidoDetalleRepository : JpaRepository<PedidoDetalleEntity, Long> {
    fun findByPedido_PedidoId(pedidoId: Long): List<PedidoDetalleEntity>
}