package com.cletaeats.auth

import com.cletaeats.cliente.ClienteEntity
import com.cletaeats.cliente.ClienteRepository
import com.cletaeats.repartidor.RepartidorEntity
import com.cletaeats.repartidor.RepartidorRepository
import com.cletaeats.rol.RolRepository
import com.cletaeats.security.JwtService
import com.cletaeats.usuario.UsuarioEntity
import com.cletaeats.usuario.UsuarioRepository
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.regex.Pattern

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val clienteRepository: ClienteRepository,
    private val rolRepository: RolRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val repartidorRepository: RepartidorRepository
) {

    private val emailPattern =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun login(request: LoginRequest): LoginResponse {
        val correo = request.correo.trim().lowercase()
        val usuario = usuarioRepository.findByCorreo(correo)
            ?: return LoginResponse(
                success = false,
                message = "Credenciales inválidas",
                token = null,
                usuarioId = null,
                clienteId = null,
                repartidorId = null,
                nombre = null,
                correo = null,
                rol = null,
                estado = null
            )

        if (usuario.estado != "ACTIVO") {
            return LoginResponse(
                success = false,
                message = "Usuario no activo",
                token = null,
                usuarioId = null,
                clienteId = null,
                repartidorId = null,
                nombre = null,
                correo = null,
                rol = null,
                estado = null
            )
        }

        val stored = usuario.passwordHash
        val passwordOk =
            if (
                stored.startsWith("\$2a\$") ||
                stored.startsWith("\$2b\$") ||
                stored.startsWith("\$2y\$")
            ) {
                passwordEncoder.matches(request.password, stored)
            } else {
                stored == request.password
            }

        if (!passwordOk) {
            return LoginResponse(
                success = false,
                message = "Credenciales inválidas",
                token = null,
                usuarioId = null,
                clienteId = null,
                repartidorId = null,
                nombre = null,
                correo = null,
                rol = null,
                estado = null
            )
        }

        val token = jwtService.generateToken(usuario)

        val clienteId = if (usuario.rol?.codigo == "CLIENTE") {
            clienteRepository.findByUsuario_UsuarioId(usuario.usuarioId!!)?.clienteId
        } else {
            null
        }

        val repartidorId = if (usuario.rol?.codigo == "REPARTIDOR") {
            repartidorRepository.findByUsuario_UsuarioId(usuario.usuarioId!!)?.repartidorId
        } else {
            null
        }

        return LoginResponse(
            success = true,
            message = "Login correcto",
            token = token,
            usuarioId = usuario.usuarioId,
            clienteId = clienteId,
            repartidorId = repartidorId,
            nombre = usuario.nombreCompleto,
            correo = usuario.correo,
            rol = usuario.rol?.codigo,
            estado = usuario.estado
        )
    }

    @Transactional
    fun register(request: RegisterRequest): BasicResponse {
        val rolCodigo = request.rol.trim().uppercase()

        if (rolCodigo !in listOf("CLIENTE", "REPARTIDOR")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido")
        }

        val nombre = request.nombre.trim()
        val cedula = request.cedula.trim()
        val correo = request.correo.trim().lowercase()
        val telefono = request.telefono.trim()
        val password = request.password.trim()

        if (nombre.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre obligatorio")
        }

        if (cedula.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cédula obligatoria")
        }

        if (correo.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo obligatorio")
        }

        if (!emailPattern.matcher(correo).matches()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo inválido")
        }

        if (telefono.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Teléfono obligatorio")
        }

        if (password.length < 6) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña inválida")
        }

        if (usuarioRepository.existsByCorreo(correo)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ya existe un usuario con ese correo"
            )
        }

        if (usuarioRepository.existsByCedula(cedula)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ya existe un usuario con esa cédula"
            )
        }

        val rol = rolRepository.findByCodigo(rolCodigo)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado")

        val usuarioGuardado = usuarioRepository.save(
            UsuarioEntity(
                rol = rol,
                nombreCompleto = nombre,
                cedula = cedula,
                correo = correo,
                telefonoCelular = telefono,
                passwordHash = passwordEncoder.encode(password).toString(),
                estado = "ACTIVO"
            )
        )

        when (rolCodigo) {
            "CLIENTE" -> {
                clienteRepository.save(
                    ClienteEntity(
                        usuario = usuarioGuardado,
                        direccionExacta = "PENDIENTE",
                        tarjetaUltimos4 = null,
                        tokenPago = null,
                        observaciones = null
                    )
                )
            }

            "REPARTIDOR" -> {
                repartidorRepository.save(
                    RepartidorEntity(
                        usuario = usuarioGuardado,
                        direccionExacta = "PENDIENTE",
                        tarjetaUltimos4 = null,
                        tokenPago = null,
                        disponibilidad = "DISPONIBLE",
                        kilometrosRecorridosDia = BigDecimal.ZERO,
                        costoKmHabil = BigDecimal("1000.00"),
                        costoKmFeriado = BigDecimal("1500.00"),
                        fotoUrl = request.fotoUrl
                    )
                )
            }
        }

        return BasicResponse(
            success = true,
            message = "Cuenta creada correctamente"
        )
    }
}