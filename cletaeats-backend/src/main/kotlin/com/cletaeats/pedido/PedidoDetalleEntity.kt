package com.cletaeats.pedido

import com.cletaeats.combo.ComboEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "PEDIDO_DETALLE")
class PedidoDetalleEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PEDIDO_DETALLE_ID")
    var pedidoDetalleId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PEDIDO_ID", nullable = false)
    var pedido: PedidoEntity? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMBO_ID", nullable = false)
    var combo: ComboEntity? = null,

    @Column(name = "CANTIDAD", nullable = false)
    var cantidad: Int = 0,

    @Column(name = "PRECIO_UNITARIO", nullable = false, precision = 10, scale = 2)
    var precioUnitario: BigDecimal = BigDecimal.ZERO
)