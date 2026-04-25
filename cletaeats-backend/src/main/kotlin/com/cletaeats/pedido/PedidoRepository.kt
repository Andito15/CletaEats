package com.cletaeats.pedido

import org.springframework.data.jpa.repository.JpaRepository

interface PedidoRepository : JpaRepository<PedidoEntity, Long> {
    fun findByCliente_ClienteIdOrderByFechaPedidoDesc(clienteId: Long): List<PedidoEntity>
    fun findByRepartidor_RepartidorIdOrderByFechaPedidoDesc(repartidorId: Long): List<PedidoEntity>
    fun findAllByOrderByFechaPedidoDesc(): List<PedidoEntity>
}