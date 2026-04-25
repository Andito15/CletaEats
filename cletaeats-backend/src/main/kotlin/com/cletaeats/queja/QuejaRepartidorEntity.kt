package com.cletaeats.queja

import com.cletaeats.cliente.ClienteEntity
import com.cletaeats.pedido.PedidoEntity
import com.cletaeats.repartidor.RepartidorEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "QUEJA_REPARTIDOR")
class QuejaRepartidorEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QUEJA_ID")
    var quejaId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PEDIDO_ID", nullable = false)
    var pedido: PedidoEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPARTIDOR_ID", nullable = false)
    var repartidor: RepartidorEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLIENTE_ID", nullable = false)
    var cliente: ClienteEntity? = null,

    @Column(name = "CATEGORIA", nullable = false, length = 50)
    var categoria: String = "",

    @Column(name = "DESCRIPCION", nullable = false, length = 500)
    var descripcion: String = "",

    @Column(name = "ESTADO", nullable = false, length = 30)
    var estado: String = "",

    @Column(name = "FECHA_QUEJA", nullable = false)
    var fechaRegistro: LocalDateTime = LocalDateTime.now()
)