package com.cletaeats.queja

import org.springframework.data.jpa.repository.JpaRepository

interface QuejaRepartidorRepository : JpaRepository<QuejaRepartidorEntity, Long> {
    fun findAllByOrderByFechaRegistroDesc(): List<QuejaRepartidorEntity>
    fun findByRepartidor_RepartidorId(repartidorId: Long): List<QuejaRepartidorEntity>
}