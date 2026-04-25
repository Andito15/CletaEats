package com.cletaeats.usuario

import com.cletaeats.rol.RolEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "USUARIO")
class UsuarioEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USUARIO_ID")
    var usuarioId: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ROL_ID", nullable = false)
    var rol: RolEntity? = null,

    @Column(name = "NOMBRE_COMPLETO", nullable = false, length = 150)
    var nombreCompleto: String = "",

    @Column(name = "CEDULA", nullable = false, unique = true, length = 20)
    var cedula: String = "",

    @Column(name = "CORREO", nullable = false, unique = true, length = 120)
    var correo: String = "",

    @Column(name = "TELEFONO_CELULAR", nullable = false, length = 20)
    var telefonoCelular: String = "",

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    var passwordHash: String = "",

    @Column(name = "ESTADO", nullable = false, length = 20)
    var estado: String = ""
)