package com.cletaeats.security

import com.cletaeats.usuario.UsuarioRepository
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val usuarioRepository: UsuarioRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val rawToken = authHeader.removePrefix("Bearer ")
        val token = rawToken.trim().replace("\\s+".toRegex(), "")

        if (token.isBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val correo = jwtService.extractCorreo(token)

            if (correo != null && SecurityContextHolder.getContext().authentication == null) {
                val usuario = usuarioRepository.findByCorreo(correo)

                if (usuario != null &&
                    usuario.estado == "ACTIVO" &&
                    jwtService.isTokenValid(token, usuario.correo)
                ) {
                    val authorities = listOf(
                        SimpleGrantedAuthority("ROLE_${usuario.rol?.codigo}")
                    )

                    val authToken = UsernamePasswordAuthenticationToken(
                        usuario.correo,
                        null,
                        authorities
                    )

                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (_: JwtException) {
            SecurityContextHolder.clearContext()
        } catch (_: IllegalArgumentException) {
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}