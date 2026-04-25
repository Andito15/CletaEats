package com.cletaeats.pedido

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/repartidores/pedidos")
class RepartidorPedidoController(
    private val pedidoService: PedidoService
) {

    @GetMapping("/mis-pedidos")
    fun listarMisPedidos(principal: Principal): List<PedidoResponse> {
        return pedidoService.listarMisPedidosRepartidor(principal.name)
    }

    @PatchMapping("/{pedidoId}/estado")
    fun actualizarEstado(
        principal: Principal,
        @PathVariable pedidoId: Long,
        @Valid @RequestBody request: PedidoEstadoRequest
    ): PedidoResponse {
        return pedidoService.actualizarEstadoRepartidor(principal.name, pedidoId, request)
    }

    @GetMapping("/{pedidoId}")
    fun obtenerPorId(@PathVariable pedidoId: Long): PedidoResponse {
        return pedidoService.obtenerPedidoPorId(pedidoId)
    }
}