package com.cletaeats.calificacion

import com.cletaeats.cliente.ClienteEntity
import com.cletaeats.pedido.PedidoEntity
import com.cletaeats.repartidor.RepartidorEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "CALIFICACION_REPARTIDOR")
class CalificacionRepartidorEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CALIFICACION_ID")
    var calificacionId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PEDIDO_ID", nullable = false)
    var pedido: PedidoEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPARTIDOR_ID", nullable = false)
    var repartidor: RepartidorEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLIENTE_ID", nullable = false)
    var cliente: ClienteEntity? = null,

    @Column(name = "PUNTAJE_AMABILIDAD", nullable = false)
    var puntajeAmabilidad: Int = 0,

    @Column(name = "PUNTAJE_TIEMPO", nullable = false)
    var puntajeTiempo: Int = 0,

    @Column(name = "PUNTAJE_PRESENTACION", nullable = false)
    var puntajePresentacion: Int = 0,

    @Column(name = "COMENTARIO", length = 500)
    var comentario: String? = null,

    @Column(name = "FECHA_REGISTRO", nullable = false)
    var fechaRegistro: LocalDateTime = LocalDateTime.now()
)