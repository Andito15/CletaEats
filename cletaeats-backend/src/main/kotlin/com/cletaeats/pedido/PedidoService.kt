package com.cletaeats.pedido

import com.cletaeats.amonestacion.AmonestacionRepartidorRepository
import com.cletaeats.cliente.ClienteRepository
import com.cletaeats.combo.ComboRepository
import com.cletaeats.factura.FacturaEntity
import com.cletaeats.factura.FacturaRepository
import com.cletaeats.feriado.FeriadoRepository
import com.cletaeats.repartidor.RepartidorEntity
import com.cletaeats.repartidor.RepartidorRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class PedidoService(
    private val pedidoRepository: PedidoRepository,
    private val pedidoDetalleRepository: PedidoDetalleRepository,
    private val facturaRepository: FacturaRepository,
    private val clienteRepository: ClienteRepository,
    private val comboRepository: ComboRepository,
    private val repartidorRepository: RepartidorRepository,
    private val amonestacionRepository: AmonestacionRepartidorRepository,
    private val feriadoRepository: FeriadoRepository
) {

    @Transactional
    fun crearPedido(correoCliente: String, request: PedidoCreateRequest): PedidoResponse {
        val cliente = clienteRepository.findByUsuario_Correo(correoCliente)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")

        if (cliente.usuario?.estado != "ACTIVO") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El cliente no está activo")
        }

        if (request.items.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido debe tener al menos un item")
        }

        val combos = request.items.map { item ->
            comboRepository.findById(item.comboId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Combo no encontrado: ${item.comboId}")
            }
        }

        if (combos.any { it.estado != "ACTIVO" }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Todos los combos deben estar activos")
        }

        val restauranteIds = combos.mapNotNull { it.restaurante?.restauranteId }.distinct()

        if (restauranteIds.size != 1) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Todos los combos del pedido deben ser del mismo restaurante"
            )
        }

        val restaurante = combos.first().restaurante
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Combo sin restaurante asociado")

        if (restaurante.estado != "ACTIVO") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El restaurante no está activo")
        }

        val hoy = LocalDate.now()
        val esFeriado = feriadoRepository.existsByFecha(hoy)
        val tipoTarifaDia = if (esFeriado) "F" else "H"

        val costoKmTemporal = if (esFeriado) {
            BigDecimal("1500.00")
        } else {
            BigDecimal("1000.00")
        }

        val pedido = PedidoEntity(
            numeroPedido = generarNumeroPedido(),
            cliente = cliente,
            restaurante = restaurante,
            repartidor = null,
            estado = "PENDIENTE_REPARTIDOR",
            fechaPedido = LocalDateTime.now(),
            fechaEntrega = null,
            direccionEntrega = request.direccionEntrega.trim(),
            distanciaKm = request.distanciaKm.setScale(2, RoundingMode.HALF_UP),
            tipoTarifaDia = tipoTarifaDia,
            costoKmAplicado = costoKmTemporal.setScale(2, RoundingMode.HALF_UP),
            observaciones = request.observaciones?.trim()
        )

        val pedidoGuardado = pedidoRepository.save(pedido)

        val detalles = request.items.map { item ->
            val combo = combos.first { it.comboId == item.comboId }

            PedidoDetalleEntity(
                pedido = pedidoGuardado,
                combo = combo,
                cantidad = item.cantidad,
                precioUnitario = combo.precio.setScale(2, RoundingMode.HALF_UP)
            )
        }

        pedidoDetalleRepository.saveAll(detalles)

        val factura = crearFacturaParaPedido(
            pedido = pedidoGuardado,
            detalles = detalles
        )

        facturaRepository.save(factura)

        return construirPedidoResponse(
            pedido = pedidoGuardado,
            detalles = detalles,
            factura = factura
        )
    }

    fun listarMisPedidosCliente(correoCliente: String): List<PedidoResponse> {
        val cliente = clienteRepository.findByUsuario_Correo(correoCliente)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")

        return pedidoRepository.findByCliente_ClienteIdOrderByFechaPedidoDesc(cliente.clienteId!!)
            .map { mapPedidoCompleto(it) }
    }

    fun listarMisPedidosRepartidor(correoRepartidor: String): List<PedidoResponse> {
        val repartidor = repartidorRepository.findByUsuario_Correo(correoRepartidor)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Repartidor no encontrado")

        return pedidoRepository.findByRepartidor_RepartidorIdOrderByFechaPedidoDesc(repartidor.repartidorId!!)
            .map { mapPedidoCompleto(it) }
    }

    fun listarPedidosDisponiblesRepartidor(correoRepartidor: String): List<PedidoResponse> {
        val repartidor = repartidorRepository.findByUsuario_Correo(correoRepartidor)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Repartidor no encontrado")

        validarRepartidorPuedeAceptar(repartidor)

        return pedidoRepository
            .findByEstadoAndRepartidorIsNullOrderByFechaPedidoAsc("PENDIENTE_REPARTIDOR")
            .map { mapPedidoCompleto(it) }
    }

    @Transactional
    fun aceptarPedidoRepartidor(
        correoRepartidor: String,
        pedidoId: Long
    ): PedidoResponse {
        val repartidor = repartidorRepository.findByUsuario_Correo(correoRepartidor)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Repartidor no encontrado")

        validarRepartidorPuedeAceptar(repartidor)

        val pedido = pedidoRepository.findById(pedidoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado")
        }

        if (pedido.repartidor != null || pedido.estado != "PENDIENTE_REPARTIDOR") {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Este pedido ya fue aceptado por otro repartidor"
            )
        }

        val costoKmAplicado = if (pedido.tipoTarifaDia == "F") {
            repartidor.costoKmFeriado
        } else {
            repartidor.costoKmHabil
        }.setScale(2, RoundingMode.HALF_UP)

        pedido.repartidor = repartidor
        pedido.estado = "EN_PREPARACION"
        pedido.costoKmAplicado = costoKmAplicado

        repartidor.disponibilidad = "OCUPADO"

        val pedidoActualizado = pedidoRepository.save(pedido)
        repartidorRepository.save(repartidor)

        recalcularFactura(pedidoActualizado)

        return mapPedidoCompleto(pedidoActualizado)
    }

    @Transactional
    fun actualizarEstadoRepartidor(
        correoRepartidor: String,
        pedidoId: Long,
        request: PedidoEstadoRequest
    ): PedidoResponse {
        val repartidor = repartidorRepository.findByUsuario_Correo(correoRepartidor)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Repartidor no encontrado")

        val pedido = pedidoRepository.findById(pedidoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado")
        }

        if (pedido.repartidor?.repartidorId != repartidor.repartidorId) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Este pedido no pertenece al repartidor autenticado"
            )
        }

        val nuevoEstado = request.estado.trim().uppercase()

        if (nuevoEstado !in listOf("EN_PREPARACION", "EN_CAMINO", "ENTREGADO")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Estado inválido. Use EN_PREPARACION, EN_CAMINO o ENTREGADO"
            )
        }

        pedido.estado = nuevoEstado

        if (nuevoEstado == "ENTREGADO") {
            pedido.fechaEntrega = LocalDateTime.now()
            repartidor.disponibilidad = "DISPONIBLE"
            repartidor.kilometrosRecorridosDia = repartidor.kilometrosRecorridosDia
                .add(pedido.distanciaKm)
                .setScale(2, RoundingMode.HALF_UP)

            repartidorRepository.save(repartidor)
        }

        val pedidoActualizado = pedidoRepository.save(pedido)

        return mapPedidoCompleto(pedidoActualizado)
    }

    fun obtenerPedidoPorId(pedidoId: Long): PedidoResponse {
        val pedido = pedidoRepository.findById(pedidoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado")
        }

        return mapPedidoCompleto(pedido)
    }

    fun listarTodosAdmin(): List<PedidoResponse> {
        return pedidoRepository.findAllByOrderByFechaPedidoDesc()
            .map { mapPedidoCompleto(it) }
    }

    private fun validarRepartidorPuedeAceptar(repartidor: RepartidorEntity) {
        if (repartidor.usuario?.estado != "ACTIVO") {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El usuario repartidor no está activo"
            )
        }

        if (repartidor.disponibilidad != "DISPONIBLE") {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "El repartidor no está disponible para aceptar pedidos"
            )
        }

        val amonestaciones = amonestacionRepository
            .countByRepartidor_RepartidorIdAndActiva(repartidor.repartidorId!!, "S")

        if (amonestaciones >= 4) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "El repartidor tiene demasiadas amonestaciones activas"
            )
        }
    }

    private fun crearFacturaParaPedido(
        pedido: PedidoEntity,
        detalles: List<PedidoDetalleEntity>
    ): FacturaEntity {
        val subtotal = calcularSubtotal(detalles)
        val costoTransporte = calcularCostoTransporte(pedido)
        val porcentajeIva = BigDecimal("13.00")
        val montoIva = calcularIva(subtotal, costoTransporte)
        val montoTotal = subtotal
            .add(costoTransporte)
            .add(montoIva)
            .setScale(2, RoundingMode.HALF_UP)

        return FacturaEntity(
            pedido = pedido,
            numeroFactura = generarNumeroFactura(),
            subtotal = subtotal,
            costoTransporte = costoTransporte,
            porcentajeIva = porcentajeIva,
            montoIva = montoIva,
            montoTotal = montoTotal,
            estadoPago = "PAGADO",
            medioPago = "TARJETA"
        )
    }

    private fun recalcularFactura(pedido: PedidoEntity) {
        val detalles = pedidoDetalleRepository.findByPedido_PedidoId(pedido.pedidoId!!)
        val factura = facturaRepository.findByPedido_PedidoId(pedido.pedidoId!!)
            ?: return

        val subtotal = calcularSubtotal(detalles)
        val costoTransporte = calcularCostoTransporte(pedido)
        val montoIva = calcularIva(subtotal, costoTransporte)
        val montoTotal = subtotal
            .add(costoTransporte)
            .add(montoIva)
            .setScale(2, RoundingMode.HALF_UP)

        factura.subtotal = subtotal
        factura.costoTransporte = costoTransporte
        factura.porcentajeIva = BigDecimal("13.00")
        factura.montoIva = montoIva
        factura.montoTotal = montoTotal

        facturaRepository.save(factura)
    }

    private fun calcularSubtotal(
        detalles: List<PedidoDetalleEntity>
    ): BigDecimal {
        return detalles.fold(BigDecimal.ZERO) { acc, detalle ->
            acc + detalle.precioUnitario.multiply(BigDecimal(detalle.cantidad))
        }.setScale(2, RoundingMode.HALF_UP)
    }

    private fun calcularCostoTransporte(
        pedido: PedidoEntity
    ): BigDecimal {
        return pedido.distanciaKm
            .multiply(pedido.costoKmAplicado)
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun calcularIva(
        subtotal: BigDecimal,
        costoTransporte: BigDecimal
    ): BigDecimal {
        return subtotal
            .add(costoTransporte)
            .multiply(BigDecimal("0.13"))
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun mapPedidoCompleto(pedido: PedidoEntity): PedidoResponse {
        val detalles = pedidoDetalleRepository.findByPedido_PedidoId(pedido.pedidoId!!)
        val factura = facturaRepository.findByPedido_PedidoId(pedido.pedidoId!!)

        return construirPedidoResponse(
            pedido = pedido,
            detalles = detalles,
            factura = factura
        )
    }

    private fun construirPedidoResponse(
        pedido: PedidoEntity,
        detalles: List<PedidoDetalleEntity>,
        factura: FacturaEntity?
    ): PedidoResponse {
        return PedidoResponse(
            pedidoId = pedido.pedidoId,
            numeroPedido = pedido.numeroPedido,
            estado = pedido.estado,
            fechaPedido = pedido.fechaPedido,
            fechaEntrega = pedido.fechaEntrega,
            clienteId = pedido.cliente?.clienteId,
            clienteNombre = pedido.cliente?.usuario?.nombreCompleto,
            restauranteId = pedido.restaurante?.restauranteId,
            restauranteNombre = pedido.restaurante?.nombre,
            repartidorId = pedido.repartidor?.repartidorId,
            repartidorNombre = pedido.repartidor?.usuario?.nombreCompleto,
            direccionEntrega = pedido.direccionEntrega,
            distanciaKm = pedido.distanciaKm,
            tipoTarifaDia = pedido.tipoTarifaDia,
            costoKmAplicado = pedido.costoKmAplicado,
            observaciones = pedido.observaciones,
            items = detalles.map {
                PedidoItemResponse(
                    comboId = it.combo?.comboId,
                    numeroCombo = it.combo?.numeroCombo ?: 0,
                    nombre = it.combo?.nombre ?: "",
                    cantidad = it.cantidad,
                    precioUnitario = it.precioUnitario,
                    subtotalLinea = it.precioUnitario
                        .multiply(BigDecimal(it.cantidad))
                        .setScale(2, RoundingMode.HALF_UP)
                )
            },
            factura = factura?.let {
                FacturaResumenResponse(
                    numeroFactura = it.numeroFactura,
                    subtotal = it.subtotal,
                    costoTransporte = it.costoTransporte,
                    porcentajeIva = it.porcentajeIva,
                    montoIva = it.montoIva,
                    montoTotal = it.montoTotal,
                    estadoPago = it.estadoPago,
                    medioPago = it.medioPago
                )
            }
        )
    }

    private fun generarNumeroPedido(): String {
        val stamp = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmssSSS")
            .format(LocalDateTime.now())

        val suffix = UUID.randomUUID()
            .toString()
            .substring(0, 6)
            .uppercase()

        return "PED-$stamp-$suffix"
    }

    private fun generarNumeroFactura(): String {
        val stamp = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmssSSS")
            .format(LocalDateTime.now())

        val suffix = UUID.randomUUID()
            .toString()
            .substring(0, 6)
            .uppercase()

        return "FAC-$stamp-$suffix"
    }
}