package com.cletaeats.repartidor

import org.springframework.data.jpa.repository.JpaRepository

interface RepartidorRepository : JpaRepository<RepartidorEntity, Long> {
    fun findByUsuario_Correo(correo: String): RepartidorEntity?
    fun findByDisponibilidadOrderByRepartidorIdAsc(disponibilidad: String): List<RepartidorEntity>
    fun findByUsuario_UsuarioId(usuarioId: Long): RepartidorEntity?
}