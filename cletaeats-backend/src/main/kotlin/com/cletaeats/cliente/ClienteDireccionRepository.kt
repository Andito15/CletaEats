package com.cletaeats.cliente

import org.springframework.data.jpa.repository.JpaRepository

interface ClienteDireccionRepository : JpaRepository<ClienteDireccionEntity, Long> {
    fun findByCliente_ClienteIdAndActivaOrderByEsPredeterminadaDescAliasAsc(
        clienteId: Long,
        activa: String
    ): List<ClienteDireccionEntity>

    fun findByDireccionIdAndCliente_ClienteId(
        direccionId: Long,
        clienteId: Long
    ): ClienteDireccionEntity?

    fun countByCliente_ClienteIdAndEsPredeterminadaAndActiva(
        clienteId: Long,
        esPredeterminada: String,
        activa: String
    ): Long
}