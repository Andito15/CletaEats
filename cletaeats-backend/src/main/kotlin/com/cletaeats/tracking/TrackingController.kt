package com.cletaeats.tracking

import com.cletaeats.tracking.dto.UbicacionRepartidorRequest
import com.cletaeats.tracking.dto.UbicacionRepartidorResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class TrackingController(
    private val trackingService: TrackingService
) {


    @PatchMapping("/repartidores/ubicacion")
    fun actualizarUbicacionRepartidor(
        authentication: Authentication,
        @RequestBody request: UbicacionRepartidorRequest
    ): ResponseEntity<UbicacionRepartidorResponse> {
        return ResponseEntity.ok(
            trackingService.actualizarUbicacion(
                authName = authentication.name,
                request = request
            )
        )
    }

    @GetMapping("/clientes/pedidos/{pedidoId}/tracking")
    fun obtenerTrackingPedido(
        authentication: Authentication,
        @PathVariable pedidoId: Long
    ): ResponseEntity<UbicacionRepartidorResponse> {
        return ResponseEntity.ok(
            trackingService.obtenerTrackingCliente(
                authName = authentication.name,
                pedidoId = pedidoId
            )
        )
    }
}