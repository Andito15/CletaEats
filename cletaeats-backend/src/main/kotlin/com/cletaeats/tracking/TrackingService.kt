package com.cletaeats.tracking

import com.cletaeats.pedido.PedidoRepository
import com.cletaeats.repartidor.RepartidorRepository
import com.cletaeats.tracking.dto.UbicacionRepartidorRequest
import com.cletaeats.tracking.dto.UbicacionRepartidorResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class TrackingService(
    private val trackingRepository: TrackingRepository,
    private val repartidorRepository: RepartidorRepository,
    private val pedidoRepository: PedidoRepository
) {

    @Transactional
    fun actualizarUbicacion(
        authName: String,
        request: UbicacionRepartidorRequest
    ): UbicacionRepartidorResponse {
        val repartidor = repartidorRepository.findByUsuario_Correo(authName)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Repartidor no encontrado"
            )

        val pedido = pedidoRepository.findById(request.pedidoId).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Pedido no encontrado"
            )
        }

        if (pedido.repartidor?.repartidorId != repartidor.repartidorId) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Este pedido no pertenece al repartidor autenticado"
            )
        }

        val estadoPedido = pedido.estado.trim().uppercase()

        if (estadoPedido != "EN_CAMINO") {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "El tracking solo está activo cuando el pedido está EN_CAMINO"
            )
        }

        repartidor.latitudActual = request.latitud
        repartidor.longitudActual = request.longitud
        repartidor.precisionMetros = request.precisionMetros
        repartidor.ultimaUbicacionEn = LocalDateTime.now()

        val guardado = repartidorRepository.save(repartidor)

        return UbicacionRepartidorResponse(
            pedidoId = pedido.pedidoId ?: request.pedidoId,
            estadoPedido = pedido.estado,
            repartidorId = guardado.repartidorId ?: repartidor.repartidorId,
            repartidorNombre = guardado.usuario?.nombreCompleto ?: "Repartidor",
            latitud = guardado.latitudActual ?: request.latitud,
            longitud = guardado.longitudActual ?: request.longitud,
            precisionMetros = guardado.precisionMetros ?: request.precisionMetros,
            ultimaUbicacionEn = guardado.ultimaUbicacionEn ?: LocalDateTime.now()
        )
    }

    @Transactional(readOnly = true)
    fun obtenerTrackingCliente(
        authName: String,
        pedidoId: Long
    ): UbicacionRepartidorResponse {
        return trackingRepository.obtenerTrackingCliente(
            authName = authName,
            pedidoId = pedidoId
        )
    }
}