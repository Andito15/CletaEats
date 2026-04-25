package com.cletaeats.admin

import com.cletaeats.amonestacion.AmonestacionRepartidorRepository
import com.cletaeats.cliente.ClienteEntity
import com.cletaeats.cliente.ClienteRepository
import com.cletaeats.repartidor.RepartidorEntity
import com.cletaeats.repartidor.RepartidorRepository
import com.cletaeats.rol.RolRepository
import com.cletaeats.usuario.UsuarioEntity
import com.cletaeats.usuario.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@Service
class AdminUsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val clienteRepository: ClienteRepository,
    private val repartidorRepository: RepartidorRepository,
    private val amonestacionRepository: AmonestacionRepartidorRepository,
    private val rolRepository: RolRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun listarUsuarios(): List<AdminUsuarioResponse> {
        return usuarioRepository.findAll()
            .sortedBy { it.nombreCompleto.lowercase() }
            .map {
                AdminUsuarioResponse(
                    usuarioId = it.usuarioId,
                    nombre = it.nombreCompleto,
                    correo = it.correo,
                    cedula = it.cedula,
                    telefono = it.telefonoCelular,
                    rol = it.rol?.codigo ?: "",
                    estado = it.estado
                )
            }
    }

    fun listarClientes(): List<AdminClienteResponse> {
        return clienteRepository.findAll()
            .sortedBy { it.usuario?.nombreCompleto?.lowercase() ?: "" }
            .map {
                AdminClienteResponse(
                    clienteId = it.clienteId,
                    usuarioId = it.usuario?.usuarioId,
                    nombre = it.usuario?.nombreCompleto ?: "",
                    correo = it.usuario?.correo ?: "",
                    cedula = it.usuario?.cedula ?: "",
                    telefono = it.usuario?.telefonoCelular ?: "",
                    estado = it.usuario?.estado ?: "",
                    direccionExacta = it.direccionExacta
                )
            }
    }

    fun listarRepartidores(): List<AdminRepartidorResponse> {
        return repartidorRepository.findAll()
            .sortedBy { it.usuario?.nombreCompleto?.lowercase() ?: "" }
            .map {
                val repartidorId = it.repartidorId
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Repartidor sin ID")

                AdminRepartidorResponse(
                    repartidorId = repartidorId,
                    usuarioId = it.usuario?.usuarioId,
                    nombre = it.usuario?.nombreCompleto ?: "",
                    correo = it.usuario?.correo ?: "",
                    cedula = it.usuario?.cedula ?: "",
                    telefono = it.usuario?.telefonoCelular ?: "",
                    estadoUsuario = it.usuario?.estado ?: "",
                    disponibilidad = it.disponibilidad,
                    kilometrosRecorridosDia = it.kilometrosRecorridosDia,
                    amonestacionesActivas = amonestacionRepository
                        .countByRepartidor_RepartidorIdAndActiva(repartidorId, "S")
                )
            }
    }

    @Transactional
    fun crearUsuario(request: AdminCreateUserRequest): AdminUsuarioResponse {
        val rolCodigo = request.rol.trim().uppercase()

        if (rolCodigo !in listOf("ADMIN", "CLIENTE", "REPARTIDOR")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Rol inválido. Use ADMIN, CLIENTE o REPARTIDOR"
            )
        }

        val correo = request.correo.trim().lowercase()
        val cedula = request.cedula.trim()

        if (usuarioRepository.existsByCorreo(correo)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un usuario con ese correo")
        }

        if (usuarioRepository.existsByCedula(cedula)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un usuario con esa cédula")
        }

        val rol = rolRepository.findByCodigo(rolCodigo)
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Rol no encontrado en la base de datos"
            )

        val passwordPlano = request.password.trim()
        if (passwordPlano.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La contraseña es obligatoria"
            )
        }

        val passwordHashGenerado = passwordEncoder.encode(passwordPlano)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo generar el hash de la contraseña"
            )

        val usuario = UsuarioEntity(
            rol = rol,
            nombreCompleto = request.nombre.trim(),
            cedula = cedula,
            correo = correo,
            telefonoCelular = request.telefono.trim(),
            passwordHash = passwordHashGenerado,
            estado = "ACTIVO"
        )

        val usuarioGuardado = usuarioRepository.save(usuario)

        when (rolCodigo) {
            "CLIENTE" -> {
                val direccion = request.direccionExacta?.trim()
                if (direccion.isNullOrBlank()) {
                    throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La dirección exacta es obligatoria para CLIENTE"
                    )
                }

                clienteRepository.save(
                    ClienteEntity(
                        usuario = usuarioGuardado,
                        direccionExacta = direccion,
                        tarjetaUltimos4 = null,
                        tokenPago = null,
                        observaciones = null
                    )
                )
            }

            "REPARTIDOR" -> {
                val direccion = request.direccionExacta?.trim()
                if (direccion.isNullOrBlank()) {
                    throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La dirección exacta es obligatoria para REPARTIDOR"
                    )
                }

                val disponibilidad = request.disponibilidad?.trim()?.uppercase() ?: "DISPONIBLE"
                if (disponibilidad !in listOf("DISPONIBLE", "OCUPADO")) {
                    throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Disponibilidad inválida. Use DISPONIBLE u OCUPADO"
                    )
                }

                repartidorRepository.save(
                    RepartidorEntity(
                        usuario = usuarioGuardado,
                        direccionExacta = direccion,
                        tarjetaUltimos4 = null,
                        tokenPago = null,
                        disponibilidad = disponibilidad,
                        kilometrosRecorridosDia = BigDecimal.ZERO,
                        costoKmHabil = BigDecimal("1000.00"),
                        costoKmFeriado = BigDecimal("1500.00"),
                        fotoUrl = request.fotoUrl?.trim()
                    )
                )
            }
        }

        return AdminUsuarioResponse(
            usuarioId = usuarioGuardado.usuarioId,
            nombre = usuarioGuardado.nombreCompleto,
            correo = usuarioGuardado.correo,
            cedula = usuarioGuardado.cedula,
            telefono = usuarioGuardado.telefonoCelular,
            rol = usuarioGuardado.rol?.codigo ?: "",
            estado = usuarioGuardado.estado
        )
    }

    @Transactional
    fun cambiarEstadoUsuario(usuarioId: Long, request: UsuarioEstadoRequest): AdminUsuarioResponse {
        val usuario = usuarioRepository.findById(usuarioId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
        }

        val estado = request.estado.trim().uppercase()
        if (estado !in listOf("ACTIVO", "INACTIVO", "SUSPENDIDO")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Estado inválido. Use ACTIVO, INACTIVO o SUSPENDIDO"
            )
        }

        usuario.estado = estado
        val saved = usuarioRepository.save(usuario)

        return AdminUsuarioResponse(
            usuarioId = saved.usuarioId,
            nombre = saved.nombreCompleto,
            correo = saved.correo,
            cedula = saved.cedula,
            telefono = saved.telefonoCelular,
            rol = saved.rol?.codigo ?: "",
            estado = saved.estado
        )
    }

    @Transactional
    fun cambiarSuspensionCliente(clienteId: Long, request: ClienteSuspensionRequest): AdminClienteResponse {
        val cliente = clienteRepository.findById(clienteId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")
        }

        val usuario = cliente.usuario
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente sin usuario asociado")

        usuario.estado = if (request.suspendido) "SUSPENDIDO" else "ACTIVO"
        usuarioRepository.save(usuario)

        return AdminClienteResponse(
            clienteId = cliente.clienteId,
            usuarioId = usuario.usuarioId,
            nombre = usuario.nombreCompleto,
            correo = usuario.correo,
            cedula = usuario.cedula,
            telefono = usuario.telefonoCelular,
            estado = usuario.estado,
            direccionExacta = cliente.direccionExacta
        )
    }

    @Transactional
    fun cambiarDisponibilidadRepartidor(
        repartidorId: Long,
        request: RepartidorDisponibilidadRequest
    ): AdminRepartidorResponse {
        val repartidor = repartidorRepository.findById(repartidorId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Repartidor no encontrado")
        }

        val disponibilidad = request.disponibilidad.trim().uppercase()
        if (disponibilidad !in listOf("DISPONIBLE", "OCUPADO")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Disponibilidad inválida. Use DISPONIBLE u OCUPADO"
            )
        }

        repartidor.disponibilidad = disponibilidad
        val saved = repartidorRepository.save(repartidor)

        val savedId = saved.repartidorId
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Repartidor sin ID")

        return AdminRepartidorResponse(
            repartidorId = savedId,
            usuarioId = saved.usuario?.usuarioId,
            nombre = saved.usuario?.nombreCompleto ?: "",
            correo = saved.usuario?.correo ?: "",
            cedula = saved.usuario?.cedula ?: "",
            telefono = saved.usuario?.telefonoCelular ?: "",
            estadoUsuario = saved.usuario?.estado ?: "",
            disponibilidad = saved.disponibilidad,
            kilometrosRecorridosDia = saved.kilometrosRecorridosDia,
            amonestacionesActivas = amonestacionRepository
                .countByRepartidor_RepartidorIdAndActiva(savedId, "S")
        )
    }

    @Transactional
    fun actualizarUsuario(usuarioId: Long, request: AdminUpdateUserRequest): AdminUsuarioResponse {
        val usuario = usuarioRepository.findById(usuarioId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
        }

        val correo = request.correo.trim().lowercase()
        val cedula = request.cedula.trim()

        if (usuario.correo != correo && usuarioRepository.existsByCorreo(correo)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un usuario con ese correo")
        }

        if (usuario.cedula != cedula && usuarioRepository.existsByCedula(cedula)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un usuario con esa cédula")
        }

        usuario.nombreCompleto = request.nombre.trim()
        usuario.cedula = cedula
        usuario.correo = correo
        usuario.telefonoCelular = request.telefono.trim()

        val rolCodigo = usuario.rol?.codigo ?: ""

        when (rolCodigo) {
            "CLIENTE" -> {
                val cliente = clienteRepository.findByUsuario_UsuarioId(usuarioId)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil de cliente no encontrado")

                val direccion = request.direccionExacta?.trim()
                if (direccion.isNullOrBlank()) {
                    throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La dirección exacta es obligatoria para CLIENTE"
                    )
                }

                cliente.direccionExacta = direccion
                clienteRepository.save(cliente)
            }

            "REPARTIDOR" -> {
                val repartidor = repartidorRepository.findByUsuario_UsuarioId(usuarioId)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil de repartidor no encontrado")

                val direccion = request.direccionExacta?.trim()
                if (direccion.isNullOrBlank()) {
                    throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La dirección exacta es obligatoria para REPARTIDOR"
                    )
                }

                val disponibilidad = request.disponibilidad?.trim()?.uppercase() ?: repartidor.disponibilidad
                if (disponibilidad !in listOf("DISPONIBLE", "OCUPADO")) {
                    throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Disponibilidad inválida. Use DISPONIBLE u OCUPADO"
                    )
                }

                repartidor.direccionExacta = direccion
                repartidor.disponibilidad = disponibilidad
                repartidor.fotoUrl = request.fotoUrl?.trim()
                repartidorRepository.save(repartidor)
            }
        }

        val saved = usuarioRepository.save(usuario)

        return AdminUsuarioResponse(
            usuarioId = saved.usuarioId,
            nombre = saved.nombreCompleto,
            correo = saved.correo,
            cedula = saved.cedula,
            telefono = saved.telefonoCelular,
            rol = saved.rol?.codigo ?: "",
            estado = saved.estado
        )
    }
}