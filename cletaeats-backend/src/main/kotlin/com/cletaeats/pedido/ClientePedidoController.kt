package com.cletaeats.pedido

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/clientes/pedidos")
class ClientePedidoController(
    private val pedidoService: PedidoService
) {

    @PostMapping
    fun crearPedido(
        principal: Principal,
        @Valid @RequestBody request: PedidoCreateRequest
    ): PedidoResponse {
        return pedidoService.crearPedido(principal.name, request)
    }

    @GetMapping("/mis-pedidos")
    fun listarMisPedidos(principal: Principal): List<PedidoResponse> {
        return pedidoService.listarMisPedidosCliente(principal.name)
    }

    @GetMapping("/{pedidoId}")
    fun obtenerPorId(@PathVariable pedidoId: Long): PedidoResponse {
        return pedidoService.obtenerPedidoPorId(pedidoId)
    }
}