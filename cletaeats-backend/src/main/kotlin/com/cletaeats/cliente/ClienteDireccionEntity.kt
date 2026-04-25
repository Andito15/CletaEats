package com.cletaeats.cliente

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "CLIENTE_DIRECCION")
class ClienteDireccionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DIRECCION_ID")
    var direccionId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLIENTE_ID", nullable = false)
    var cliente: ClienteEntity,

    @Column(name = "ALIAS", nullable = false)
    var alias: String,

    @Column(name = "DIRECCION_TEXTO", nullable = false)
    var direccionTexto: String,

    @Column(name = "LATITUD", nullable = false)
    var latitud: Double,

    @Column(name = "LONGITUD", nullable = false)
    var longitud: Double,

    @Column(name = "ES_PREDETERMINADA", nullable = false)
    var esPredeterminada: String = "N",

    @Column(name = "ACTIVA", nullable = false)
    var activa: String = "S",

    @Column(name = "CREATED_AT", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "UPDATED_AT")
    var updatedAt: LocalDateTime? = null
)