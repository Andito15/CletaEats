package com.cletaeats.security

import com.cletaeats.usuario.UsuarioEntity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long
) {

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(
        Base64.getDecoder().decode(secret)
    )

    fun generateToken(usuario: UsuarioEntity): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(usuario.correo)
            .claim("userId", usuario.usuarioId)
            .claim("rol", usuario.rol?.codigo)
            .claim("nombre", usuario.nombreCompleto)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    fun extractCorreo(token: String): String? {
        return extractAllClaims(token).subject
    }

    fun extractRol(token: String): String? {
        return extractAllClaims(token)["rol"] as? String
    }

    fun isTokenValid(token: String, correo: String): Boolean {
        val claims = extractAllClaims(token)
        val notExpired = claims.expiration.after(Date())
        return claims.subject == correo && notExpired
    }

    private fun extractAllClaims(token: String): Claims {
        val cleanToken = token.trim().replace("\\s+".toRegex(), "")

        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(cleanToken)
            .payload
    }
}