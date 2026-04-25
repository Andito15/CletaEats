package com.cletaeats.rol

import org.springframework.data.jpa.repository.JpaRepository

interface RolRepository : JpaRepository<RolEntity, Long> {
    fun findByCodigo(codigo: String): RolEntity?
}