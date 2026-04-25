package com.cletaeats.usuario

import org.springframework.data.jpa.repository.JpaRepository

interface UsuarioRepository : JpaRepository<UsuarioEntity, Long> {
    fun findByCorreo(correo: String): UsuarioEntity?
    fun existsByCorreo(correo: String): Boolean
    fun existsByCedula(cedula: String): Boolean
}