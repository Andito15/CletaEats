package com.cletaeats.pedido

import com.cletaeats.cliente.ClienteEntity
import com.cletaeats.repartidor.RepartidorEntity
import com.cletaeats.restaurante.RestauranteEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "PEDIDO")
class PedidoEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PEDIDO_ID")
    var pedidoId: Long? = null,

    @Column(name = "NUMERO_PEDIDO", nullable = false, unique = true, length = 30)
    var numeroPedido: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENTE_ID", nullable = false)
    var cliente: ClienteEntity? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RESTAURANTE_ID", nullable = false)
    var restaurante: RestauranteEntity? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REPARTIDOR_ID", nullable = false)
    var repartidor: RepartidorEntity? = null,

    @Column(name = "ESTADO", nullable = false, length = 30)
    var estado: String = "",

    @Column(name = "FECHA_PEDIDO", nullable = false)
    var fechaPedido: LocalDateTime = LocalDateTime.now(),

    @Column(name = "FECHA_ENTREGA")
    var fechaEntrega: LocalDateTime? = null,

    @Column(name = "DIRECCION_ENTREGA", nullable = false, length = 250)
    var direccionEntrega: String = "",

    @Column(name = "DISTANCIA_KM", nullable = false, precision = 10, scale = 2)
    var distanciaKm: BigDecimal = BigDecimal.ZERO,

    @Column(name = "TIPO_TARIFA_DIA", nullable = false, length = 1)
    var tipoTarifaDia: String = "",

    @Column(name = "COSTO_KM_APLICADO", nullable = false, precision = 10, scale = 2)
    var costoKmAplicado: BigDecimal = BigDecimal.ZERO,

    @Column(name = "OBSERVACIONES", length = 500)
    var observaciones: String? = null
)