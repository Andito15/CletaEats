package com.cletaeats.tracking

import com.cletaeats.tracking.dto.UbicacionRepartidorRequest
import com.cletaeats.tracking.dto.UbicacionRepartidorResponse

interface TrackingRepository {

    fun actualizarUbicacionRepartidor(
        authName: String,
        request: UbicacionRepartidorRequest
    ): UbicacionRepartidorResponse

    fun obtenerTrackingCliente(
        authName: String,
        pedidoId: Long
    ): UbicacionRepartidorResponse
}