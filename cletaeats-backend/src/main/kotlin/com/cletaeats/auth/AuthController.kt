package com.cletaeats.auth

import com.cletaeats.usuario.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.security.Principal

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val usuarioService: UsuarioService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse {
        return authService.login(request)
    }

    @GetMapping("/me")
    fun me(principal: Principal): MeResponse {
        return usuarioService.obtenerMe(principal.name)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
    }

    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest
    ): BasicResponse {
        return authService.register(request)
    }
}