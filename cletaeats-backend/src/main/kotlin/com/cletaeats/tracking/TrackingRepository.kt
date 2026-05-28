package com.cletaeats.tracking

import com.cletaeats.tracking.dto.UbicacionRepartidorResponse

interface TrackingRepository {

    fun obtenerTrackingCliente(
        authName: String,
        pedidoId: Long
    ): UbicacionRepartidorResponse
}