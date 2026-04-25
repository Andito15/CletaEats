package com.cletaeats.restaurante

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/restaurantes")
class AdminRestauranteController(
    private val restauranteService: RestauranteService
) {

    @PostMapping
    fun crear(@Valid @RequestBody request: RestauranteRequest): RestauranteResponse {
        return restauranteService.crear(request)
    }

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: RestauranteRequest
    ): RestauranteResponse {
        return restauranteService.actualizar(id, request)
    }

    @PatchMapping("/{id}/estado")
    fun cambiarEstado(
        @PathVariable id: Long,
        @Valid @RequestBody request: RestauranteEstadoRequest
    ): RestauranteResponse {
        return restauranteService.cambiarEstado(id, request)
    }
}