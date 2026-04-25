package com.cletaeats.restaurante

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class RestauranteService(
    private val restauranteRepository: RestauranteRepository
) {

    fun listarTodos(): List<RestauranteResponse> {
        return restauranteRepository.findAll()
            .sortedBy { it.nombre.lowercase() }
            .map { it.toResponse() }
    }

    fun listarActivos(): List<RestauranteResponse> {
        return restauranteRepository.findByEstado("ACTIVO")
            .sortedBy { it.nombre.lowercase() }
            .map { it.toResponse() }
    }

    fun obtenerPorId(id: Long): RestauranteResponse {
        val restaurante = restauranteRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante no encontrado")
            }

        return restaurante.toResponse()
    }

    fun crear(request: RestauranteRequest): RestauranteResponse {
        if (restauranteRepository.existsByCedulaJuridica(request.cedulaJuridica.trim())) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ya existe un restaurante con esa cédula jurídica"
            )
        }

        validarUbicacion(request.latitud, request.longitud)

        val restaurante = RestauranteEntity(
            nombre = request.nombre.trim(),
            cedulaJuridica = request.cedulaJuridica.trim(),
            direccion = request.direccion.trim(),
            tipoComida = request.tipoComida.trim().uppercase(),
            estado = "ACTIVO",
            imagenUrl = request.imagenUrl?.trim(),
            latitud = request.latitud,
            longitud = request.longitud,
            ubicacionActualizadaEn = LocalDateTime.now()
        )

        return restauranteRepository.save(restaurante).toResponse()
    }

    fun actualizar(id: Long, request: RestauranteRequest): RestauranteResponse {
        val restaurante = restauranteRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante no encontrado")
            }

        val nuevaCedula = request.cedulaJuridica.trim()
        if (restaurante.cedulaJuridica != nuevaCedula &&
            restauranteRepository.existsByCedulaJuridica(nuevaCedula)
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ya existe un restaurante con esa cédula jurídica"
            )
        }

        validarUbicacion(request.latitud, request.longitud)

        restaurante.nombre = request.nombre.trim()
        restaurante.cedulaJuridica = nuevaCedula
        restaurante.direccion = request.direccion.trim()
        restaurante.tipoComida = request.tipoComida.trim().uppercase()
        restaurante.imagenUrl = request.imagenUrl?.trim()
        restaurante.latitud = request.latitud
        restaurante.longitud = request.longitud
        restaurante.ubicacionActualizadaEn = LocalDateTime.now()

        return restauranteRepository.save(restaurante).toResponse()
    }

    fun cambiarEstado(id: Long, request: RestauranteEstadoRequest): RestauranteResponse {
        val restaurante = restauranteRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante no encontrado")
            }

        val estadoNormalizado = request.estado.trim().uppercase()
        if (estadoNormalizado !in listOf("ACTIVO", "INACTIVO")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Estado inválido. Use ACTIVO o INACTIVO"
            )
        }

        restaurante.estado = estadoNormalizado
        return restauranteRepository.save(restaurante).toResponse()
    }

    private fun RestauranteEntity.toResponse(): RestauranteResponse {
        return RestauranteResponse(
            id = this.restauranteId,
            nombre = this.nombre,
            cedulaJuridica = this.cedulaJuridica,
            direccion = this.direccion,
            tipoComida = this.tipoComida,
            estado = this.estado,
            imagenUrl = this.imagenUrl,
            latitud = this.latitud,
            longitud = this.longitud
        )
    }

    private fun validarUbicacion(latitud: Double?, longitud: Double?) {
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
}