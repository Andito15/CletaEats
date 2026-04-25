package com.cletaeats.usuario

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    private val usuarioService: UsuarioService
) {

    @GetMapping
    fun listarUsuarios(): List<UsuarioResponse> {
        return usuarioService.listarUsuarios()
    }
}