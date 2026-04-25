package com.cletaeats.admin

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminUsuarioController(
    private val adminUsuarioService: AdminUsuarioService
) {

    @GetMapping("/usuarios")
    fun listarUsuarios(): List<AdminUsuarioResponse> {
        return adminUsuarioService.listarUsuarios()
    }

    @GetMapping("/clientes")
    fun listarClientes(): List<AdminClienteResponse> {
        return adminUsuarioService.listarClientes()
    }

    @GetMapping("/repartidores")
    fun listarRepartidores(): List<AdminRepartidorResponse> {
        return adminUsuarioService.listarRepartidores()
    }

    @PostMapping("/usuarios")
    fun crearUsuario(
        @Valid @RequestBody request: AdminCreateUserRequest
    ): AdminUsuarioResponse {
        return adminUsuarioService.crearUsuario(request)
    }

    @PatchMapping("/usuarios/{usuarioId}/estado")
    fun cambiarEstadoUsuario(
        @PathVariable usuarioId: Long,
        @RequestBody request: UsuarioEstadoRequest
    ): AdminUsuarioResponse {
        return adminUsuarioService.cambiarEstadoUsuario(usuarioId, request)
    }

    @PatchMapping("/clientes/{clienteId}/suspension")
    fun cambiarSuspensionCliente(
        @PathVariable clienteId: Long,
        @RequestBody request: ClienteSuspensionRequest
    ): AdminClienteResponse {
        return adminUsuarioService.cambiarSuspensionCliente(clienteId, request)
    }

    @PatchMapping("/repartidores/{repartidorId}/disponibilidad")
    fun cambiarDisponibilidadRepartidor(
        @PathVariable repartidorId: Long,
        @RequestBody request: RepartidorDisponibilidadRequest
    ): AdminRepartidorResponse {
        return adminUsuarioService.cambiarDisponibilidadRepartidor(repartidorId, request)
    }

    @PutMapping("/usuarios/{usuarioId}")
    fun actualizarUsuario(
        @PathVariable usuarioId: Long,
        @Valid @RequestBody request: AdminUpdateUserRequest
    ): AdminUsuarioResponse {
        return adminUsuarioService.actualizarUsuario(usuarioId, request)
    }
}