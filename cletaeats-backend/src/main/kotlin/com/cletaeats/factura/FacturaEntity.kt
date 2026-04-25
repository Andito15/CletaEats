package com.cletaeats.factura

import com.cletaeats.pedido.PedidoEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "FACTURA")
class FacturaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FACTURA_ID")
    var facturaId: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PEDIDO_ID", nullable = false, unique = true)
    var pedido: PedidoEntity? = null,

    @Column(name = "NUMERO_FACTURA", nullable = false, unique = true, length = 30)
    var numeroFactura: String = "",

    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    var subtotal: BigDecimal = BigDecimal.ZERO,

    @Column(name = "COSTO_TRANSPORTE", nullable = false, precision = 12, scale = 2)
    var costoTransporte: BigDecimal = BigDecimal.ZERO,

    @Column(name = "PORCENTAJE_IVA", nullable = false, precision = 5, scale = 2)
    var porcentajeIva: BigDecimal = BigDecimal.ZERO,

    @Column(name = "MONTO_IVA", nullable = false, precision = 12, scale = 2)
    var montoIva: BigDecimal = BigDecimal.ZERO,

    @Column(name = "MONTO_TOTAL", nullable = false, precision = 12, scale = 2)
    var montoTotal: BigDecimal = BigDecimal.ZERO,

    @Column(name = "ESTADO_PAGO", nullable = false, length = 20)
    var estadoPago: String = "",

    @Column(name = "MEDIO_PAGO", nullable = false, length = 30)
    var medioPago: String = ""
)