package com.cletaeats.amonestacion

import org.springframework.data.jpa.repository.JpaRepository

interface AmonestacionRepartidorRepository : JpaRepository<AmonestacionRepartidorEntity, Long> {
    fun countByRepartidor_RepartidorIdAndActiva(repartidorId: Long, activa: String): Long
    fun existsByQueja_QuejaId(quejaId: Long): Boolean
    fun findByQueja_QuejaId(quejaId: Long): List<AmonestacionRepartidorEntity>
    fun findByRepartidor_RepartidorId(repartidorId: Long): List<AmonestacionRepartidorEntity>
}