package com.cletaeats.rol

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "ROL")
class RolEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROL_ID")
    var rolId: Long? = null,

    @Column(name = "CODIGO", nullable = false, unique = true, length = 30)
    var codigo: String = "",

    @Column(name = "NOMBRE", nullable = false, length = 80)
    var nombre: String = ""
)