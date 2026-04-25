package com.cletaeats.cliente

import org.springframework.data.jpa.repository.JpaRepository

interface ClienteRepository : JpaRepository<ClienteEntity, Long> {
    fun findByUsuario_Correo(correo: String): ClienteEntity?
    fun findByUsuario_UsuarioId(usuarioId: Long): ClienteEntity?
}