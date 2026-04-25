package com.cletaeats.combo

import com.cletaeats.restaurante.RestauranteRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.RoundingMode

@Service
class ComboService(
    private val comboRepository: ComboRepository,
    private val restauranteRepository: RestauranteRepository
) {

    fun listarPorRestaurante(restauranteId: Long): List<ComboResponse> {
        validarRestauranteExiste(restauranteId)

        return comboRepository.findByRestaurante_RestauranteId(restauranteId)
            .sortedBy { it.numeroCombo }
            .map { it.toResponse() }
    }

    fun listarActivosPorRestaurante(restauranteId: Long): List<ComboResponse> {
        validarRestauranteExiste(restauranteId)

        return comboRepository.findByRestaurante_RestauranteIdAndEstado(restauranteId, "ACTIVO")
            .sortedBy { it.numeroCombo }
            .map { it.toResponse() }
    }

    fun obtenerPorId(comboId: Long): ComboResponse {
        val combo = comboRepository.findById(comboId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Combo no encontrado")
            }

        return combo.toResponse()
    }

    fun crear(restauranteId: Long, request: ComboRequest): ComboResponse {
        val restaurante = restauranteRepository.findById(restauranteId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante no encontrado")
            }

        validarNumeroCombo(request.numeroCombo)

        if (comboRepository.existsByRestaurante_RestauranteIdAndNumeroCombo(restauranteId, request.numeroCombo)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ese restaurante ya tiene un combo con ese número"
            )
        }

        val combo = ComboEntity(
            restaurante = restaurante,
            numeroCombo = request.numeroCombo,
            nombre = request.nombre.trim(),
            descripcion = request.descripcion.trim(),
            precio = request.precio.setScale(2, RoundingMode.HALF_UP),
            estado = "ACTIVO",
            imagenUrl = request.imagenUrl?.trim()
        )

        return comboRepository.save(combo).toResponse()
    }

    fun actualizar(comboId: Long, request: ComboRequest): ComboResponse {
        val combo = comboRepository.findById(comboId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Combo no encontrado")
            }

        validarNumeroCombo(request.numeroCombo)

        val restauranteId = combo.restaurante?.restauranteId
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El combo no tiene restaurante asociado")

        if (combo.numeroCombo != request.numeroCombo &&
            comboRepository.existsByRestaurante_RestauranteIdAndNumeroCombo(restauranteId, request.numeroCombo)
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ese restaurante ya tiene un combo con ese número"
            )
        }

        combo.numeroCombo = request.numeroCombo
        combo.nombre = request.nombre.trim()
        combo.descripcion = request.descripcion.trim()
        combo.precio = request.precio.setScale(2, RoundingMode.HALF_UP)
        combo.imagenUrl = request.imagenUrl?.trim()

        return comboRepository.save(combo).toResponse()
    }

    fun cambiarEstado(comboId: Long, request: ComboEstadoRequest): ComboResponse {
        val combo = comboRepository.findById(comboId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Combo no encontrado")
            }

        val estado = request.estado.trim().uppercase()
        if (estado !in listOf("ACTIVO", "INACTIVO")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Estado inválido. Use ACTIVO o INACTIVO"
            )
        }

        combo.estado = estado
        return comboRepository.save(combo).toResponse()
    }

    private fun validarRestauranteExiste(restauranteId: Long) {
        if (!restauranteRepository.existsById(restauranteId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante no encontrado")
        }
    }

    private fun validarNumeroCombo(numeroCombo: Int) {
        if (numeroCombo !in 1..9) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El número de combo debe estar entre 1 y 9"
            )
        }
    }

    private fun ComboEntity.toResponse(): ComboResponse {
        return ComboResponse(
            id = this.comboId,
            restauranteId = this.restaurante?.restauranteId,
            restauranteNombre = this.restaurante?.nombre,
            numeroCombo = this.numeroCombo,
            nombre = this.nombre,
            descripcion = this.descripcion,
            precio = this.precio,
            estado = this.estado,
            imagenUrl = this.imagenUrl
        )
    }
}