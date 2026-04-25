package com.cletaeats.calificacion

import org.springframework.data.jpa.repository.JpaRepository

interface CalificacionRepartidorRepository : JpaRepository<CalificacionRepartidorEntity, Long> {
    fun existsByPedido_PedidoId(pedidoId: Long): Boolean
    fun findByRepartidor_RepartidorId(repartidorId: Long): List<CalificacionRepartidorEntity>
}