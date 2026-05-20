package com.cletaeats.pedido

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/repartidores/pedidos")
class RepartidorPedidoController(
    private val pedidoService: PedidoService
) {

    @GetMapping("/mis-pedidos")
    fun listarMisPedidos(
        principal: Principal
    ): List<PedidoResponse> {
        return pedidoService.listarMisPedidosRepartidor(principal.name)
    }

    @GetMapping("/disponibles")
    fun listarDisponibles(
        principal: Principal
    ): List<PedidoResponse> {
        return pedidoService.listarPedidosDisponiblesRepartidor(principal.name)
    }

    @PatchMapping("/{pedidoId}/aceptar")
    fun aceptarPedido(
        principal: Principal,
        @PathVariable pedidoId: Long
    ): PedidoResponse {
        return pedidoService.aceptarPedidoRepartidor(
            correoRepartidor = principal.name,
            pedidoId = pedidoId
        )
    }

    @PatchMapping("/{pedidoId}/estado")
    fun actualizarEstado(
        principal: Principal,
        @PathVariable pedidoId: Long,
        @Valid @RequestBody request: PedidoEstadoRequest
    ): PedidoResponse {
        return pedidoService.actualizarEstadoRepartidor(
            correoRepartidor = principal.name,
            pedidoId = pedidoId,
            request = request
        )
    }

    @GetMapping("/{pedidoId}")
    fun obtenerPorId(
        @PathVariable pedidoId: Long
    ): PedidoResponse {
        return pedidoService.obtenerPedidoPorId(pedidoId)
    }
}