package com.cletaeats.restaurante

import org.springframework.data.jpa.repository.JpaRepository

interface RestauranteRepository : JpaRepository<RestauranteEntity, Long> {
    fun findByEstado(estado: String): List<RestauranteEntity>
    fun existsByCedulaJuridica(cedulaJuridica: String): Boolean
}