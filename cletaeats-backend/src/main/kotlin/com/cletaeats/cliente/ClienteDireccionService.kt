package com.cletaeats.cliente

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class ClienteDireccionService(
    private val clienteRepository: ClienteRepository,
    private val clienteDireccionRepository: ClienteDireccionRepository
) {

    fun listarPorCliente(clienteId: Long): List<ClienteDireccionResponse> {
        validarClienteExiste(clienteId)

        return clienteDireccionRepository
            .findByCliente_ClienteIdAndActivaOrderByEsPredeterminadaDescAliasAsc(
                clienteId,
                "S"
            )
            .map { it.toResponse() }
    }

    @Transactional
    fun crear(clienteId: Long, request: ClienteDireccionRequest): ClienteDireccionResponse {
        val cliente = clienteRepository.findById(clienteId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")
            }

        val alias = request.alias.trim()
        val direccionTexto = request.direccionTexto.trim()
        val latitud = request.latitud
        val longitud = request.longitud
        val esPredeterminada = request.esPredeterminada == true

        validarDatos(alias, direccionTexto, latitud, longitud)

        val yaTienePredeterminada =
            clienteDireccionRepository.countByCliente_ClienteIdAndEsPredeterminadaAndActiva(
                clienteId,
                "S",
                "S"
            ) > 0

        val marcarComoPredeterminada = esPredeterminada || !yaTienePredeterminada

        if (marcarComoPredeterminada) {
            desmarcarPredeterminadas(clienteId)
        }

        val entidad = ClienteDireccionEntity(
            cliente = cliente,
            alias = alias,
            direccionTexto = direccionTexto,
            latitud = latitud!!,
            longitud = longitud!!,
            esPredeterminada = if (marcarComoPredeterminada) "S" else "N",
            activa = "S",
            createdAt = LocalDateTime.now(),
            updatedAt = null
        )

        return clienteDireccionRepository.save(entidad).toResponse()
    }

    @Transactional
    fun actualizar(
        clienteId: Long,
        direccionId: Long,
        request: ClienteDireccionRequest
    ): ClienteDireccionResponse {
        validarClienteExiste(clienteId)

        val direccion = clienteDireccionRepository.findByDireccionIdAndCliente_ClienteId(
            direccionId,
            clienteId
        ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada")

        if (direccion.activa != "S") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La dirección está inactiva")
        }

        val alias = request.alias.trim()
        val direccionTexto = request.direccionTexto.trim()
        val latitud = request.latitud
        val longitud = request.longitud
        val esPredeterminada = request.esPredeterminada == true

        validarDatos(alias, direccionTexto, latitud, longitud)

        if (esPredeterminada) {
            desmarcarPredeterminadas(clienteId)
            direccion.esPredeterminada = "S"
        } else {
            val totalPredeterminadasActivas =
                clienteDireccionRepository.countByCliente_ClienteIdAndEsPredeterminadaAndActiva(
                    clienteId,
                    "S",
                    "S"
                )

            if (direccion.esPredeterminada == "S" && totalPredeterminadasActivas <= 1) {
                direccion.esPredeterminada = "S"
            } else {
                direccion.esPredeterminada = "N"
            }
        }

        direccion.alias = alias
        direccion.direccionTexto = direccionTexto
        direccion.latitud = latitud!!
        direccion.longitud = longitud!!
        direccion.updatedAt = LocalDateTime.now()

        return clienteDireccionRepository.save(direccion).toResponse()
    }

    @Transactional
    fun eliminar(clienteId: Long, direccionId: Long) {
        validarClienteExiste(clienteId)

        val direccion = clienteDireccionRepository.findByDireccionIdAndCliente_ClienteId(
            direccionId,
            clienteId
        ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada")

        if (direccion.activa != "S") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La dirección ya está inactiva")
        }

        val eraPredeterminada = direccion.esPredeterminada == "S"

        direccion.activa = "N"
        direccion.esPredeterminada = "N"
        direccion.updatedAt = LocalDateTime.now()
        clienteDireccionRepository.save(direccion)

        if (eraPredeterminada) {
            val restantesActivas = clienteDireccionRepository
                .findByCliente_ClienteIdAndActivaOrderByEsPredeterminadaDescAliasAsc(clienteId, "S")

            val primera = restantesActivas.firstOrNull()
            if (primera != null) {
                primera.esPredeterminada = "S"
                primera.updatedAt = LocalDateTime.now()
                clienteDireccionRepository.save(primera)
            }
        }
    }

    @Transactional
    fun marcarPredeterminada(clienteId: Long, direccionId: Long): ClienteDireccionResponse {
        validarClienteExiste(clienteId)

        val direcciones = clienteDireccionRepository
            .findByCliente_ClienteIdAndActivaOrderByEsPredeterminadaDescAliasAsc(clienteId, "S")

        var encontrada: ClienteDireccionEntity? = null

        direcciones.forEach {
            if (it.direccionId == direccionId) {
                it.esPredeterminada = "S"
                encontrada = it
            } else {
                it.esPredeterminada = "N"
            }
            it.updatedAt = LocalDateTime.now()
        }

        clienteDireccionRepository.saveAll(direcciones)

        return encontrada?.toResponse()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada")
    }

    private fun validarClienteExiste(clienteId: Long) {
        if (!clienteRepository.existsById(clienteId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")
        }
    }

    private fun validarDatos(
        alias: String,
        direccionTexto: String,
        latitud: Double?,
        longitud: Double?
    ) {
        if (alias.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Alias obligatorio")
        }

        if (alias.length > 50) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Alias demasiado largo")
        }

        if (direccionTexto.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Dirección obligatoria")
        }

        if (direccionTexto.length > 300) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Dirección demasiado larga")
        }

        if (latitud == null || longitud == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ubicación obligatoria")
        }

        if (latitud < -90 || latitud > 90) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitud inválida")
        }

        if (longitud < -180 || longitud > 180) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Longitud inválida")
        }
    }

    private fun desmarcarPredeterminadas(clienteId: Long) {
        val activas = clienteDireccionRepository
            .findByCliente_ClienteIdAndActivaOrderByEsPredeterminadaDescAliasAsc(clienteId, "S")

        activas.forEach {
            if (it.esPredeterminada == "S") {
                it.esPredeterminada = "N"
                it.updatedAt = LocalDateTime.now()
                clienteDireccionRepository.save(it)
            }
        }
    }

    private fun ClienteDireccionEntity.toResponse(): ClienteDireccionResponse {
        return ClienteDireccionResponse(
            direccionId = this.direccionId,
            alias = this.alias,
            direccionTexto = this.direccionTexto,
            latitud = this.latitud,
            longitud = this.longitud,
            esPredeterminada = this.esPredeterminada == "S"
        )
    }
}