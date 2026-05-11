package com.cletaeats.tracking

import com.cletaeats.tracking.dto.UbicacionRepartidorRequest
import com.cletaeats.tracking.dto.UbicacionRepartidorResponse
import org.springframework.stereotype.Service

@Service
class TrackingService(
    private val trackingRepository: TrackingRepository
) {

    fun actualizarUbicacion(
        authName: String,
        request: UbicacionRepartidorRequest
    ): UbicacionRepartidorResponse {
        return trackingRepository.actualizarUbicacionRepartidor(
            authName = authName,
            request = request
        )
    }

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