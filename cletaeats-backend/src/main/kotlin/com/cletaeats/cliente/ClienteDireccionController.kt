package com.cletaeats.cliente

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clientes/{clienteId}/direcciones")
class ClienteDireccionController(
    private val clienteDireccionService: ClienteDireccionService
) {

    @GetMapping
    fun listar(
        @PathVariable clienteId: Long
    ): List<ClienteDireccionResponse> {
        return clienteDireccionService.listarPorCliente(clienteId)
    }

    @PostMapping
    fun crear(
        @PathVariable clienteId: Long,
        @RequestBody request: ClienteDireccionRequest
    ): ClienteDireccionResponse {
        return clienteDireccionService.crear(clienteId, request)
    }

    @PutMapping("/{direccionId}")
    fun actualizar(
        @PathVariable clienteId: Long,
        @PathVariable direccionId: Long,
        @RequestBody request: ClienteDireccionRequest
    ): ClienteDireccionResponse {
        return clienteDireccionService.actualizar(clienteId, direccionId, request)
    }

    @DeleteMapping("/{direccionId}")
    fun eliminar(
        @PathVariable clienteId: Long,
        @PathVariable direccionId: Long
    ) {
        clienteDireccionService.eliminar(clienteId, direccionId)
    }

    @PatchMapping("/{direccionId}/predeterminada")
    fun marcarPredeterminada(
        @PathVariable clienteId: Long,
        @PathVariable direccionId: Long
    ): ClienteDireccionResponse {
        return clienteDireccionService.marcarPredeterminada(clienteId, direccionId)
    }
}