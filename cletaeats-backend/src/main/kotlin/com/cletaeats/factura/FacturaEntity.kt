package com.cletaeats.factura

import com.cletaeats.pedido.PedidoEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

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

    @Column(name = "FECHA_EMISION", nullable = false)
    var fechaEmision: LocalDateTime = LocalDateTime.now(),

    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    var subtotal: BigDecimal = BigDecimal.ZERO,

    @Column(name = "COSTO_TRANSPORTE", nullable = false, precision = 12, scale = 2)
    var costoTransporte: BigDecimal = BigDecimal.ZERO,

    @Column(name = "PORCENTAJE_IVA", nullable = false, precision = 5, scale = 2)
    var porcentajeIva: BigDecimal = BigDecimal("13.00"),

    @Column(name = "MONTO_IVA", nullable = false, precision = 12, scale = 2)
    var montoIva: BigDecimal = BigDecimal.ZERO,

    @Column(name = "MONTO_TOTAL", nullable = false, precision = 12, scale = 2)
    var montoTotal: BigDecimal = BigDecimal.ZERO,

    @Column(name = "ESTADO_PAGO", nullable = false, length = 20)
    var estadoPago: String = "PAGADO",

    @Column(name = "MEDIO_PAGO", nullable = false, length = 30)
    var medioPago: String = "TARJETA",

    @Column(name = "TARJETA_RESUMEN", length = 80)
    var tarjetaResumen: String? = null
)