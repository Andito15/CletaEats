package com.cletaeats.pedido

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/pedidos")
class AdminPedidoController(
    private val pedidoService: PedidoService
) {

    @GetMapping
    fun listarTodos(): List<PedidoResponse> {
        return pedidoService.listarTodosAdmin()
    }

    @GetMapping("/{pedidoId}")
    fun obtenerPorId(@PathVariable pedidoId: Long): PedidoResponse {
        return pedidoService.obtenerPedidoPorId(pedidoId)
    }
}