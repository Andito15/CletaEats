package com.cletaeats.cliente

import com.cletaeats.usuario.UsuarioEntity
import jakarta.persistence.*

@Entity
@Table(name = "CLIENTE")
class ClienteEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLIENTE_ID")
    var clienteId: Long? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USUARIO_ID", nullable = false, unique = true)
    var usuario: UsuarioEntity? = null,

    @Column(name = "DIRECCION_EXACTA", nullable = false, length = 250)
    var direccionExacta: String = "",

    @Column(name = "TARJETA_ULTIMOS4", length = 4)
    var tarjetaUltimos4: String? = null,

    @Column(name = "TOKEN_PAGO", length = 255)
    var tokenPago: String? = null,

    @Column(name = "OBSERVACIONES", length = 500)
    var observaciones: String? = null
)