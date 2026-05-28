package com.cletaeats.tracking

import com.cletaeats.pedido.PedidoRepository
import com.cletaeats.tracking.dto.UbicacionRepartidorResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.web.server.ResponseStatusException

@Repository
class TrackingRepositoryImpl(
    private val pedidoRepository: PedidoRepository
) : TrackingRepository {

    override fun obtenerTrackingCliente(
        authName: String,
        pedidoId: Long
    ): UbicacionRepartidorResponse {
        val pedido = pedidoRepository.findById(pedidoId).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Pedido no encontrado"
            )
        }

        if (pedido.cliente?.usuario?.correo != authName) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Este pedido no pertenece al cliente autenticado"
            )
        }

        val estadoPedido = pedido.estado.trim().uppercase()

        if (estadoPedido != "EN_CAMINO") {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "El tracking solo está disponible cuando el pedido está EN_CAMINO"
            )
        }

        val repartidor = pedido.repartidor
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "El pedido todavía no tiene repartidor asignado"
            )

        val latitud = repartidor.latitudActual
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "El repartidor todavía no ha enviado ubicación"
            )

        val longitud = repartidor.longitudActual
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "El repartidor todavía no ha enviado ubicación"
            )

        val repartidorId = repartidor.repartidorId
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Repartidor sin ID"
            )

        return UbicacionRepartidorResponse(
            pedidoId = pedido.pedidoId ?: pedidoId,
            estadoPedido = pedido.estado,
            repartidorId = repartidorId,
            repartidorNombre = repartidor.usuario?.nombreCompleto ?: "Repartidor",
            latitud = latitud,
            longitud = longitud,
            precisionMetros = repartidor.precisionMetros,
            ultimaUbicacionEn = repartidor.ultimaUbicacionEn
        )
    }
}