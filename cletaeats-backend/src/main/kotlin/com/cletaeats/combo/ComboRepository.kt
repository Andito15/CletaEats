package com.cletaeats.combo

import org.springframework.data.jpa.repository.JpaRepository

interface ComboRepository : JpaRepository<ComboEntity, Long> {

    fun findByRestaurante_RestauranteId(restauranteId: Long): List<ComboEntity>

    fun findByRestaurante_RestauranteIdAndEstado(
        restauranteId: Long,
        estado: String
    ): List<ComboEntity>

    fun existsByRestaurante_RestauranteIdAndNumeroCombo(
        restauranteId: Long,
        numeroCombo: Int
    ): Boolean
}