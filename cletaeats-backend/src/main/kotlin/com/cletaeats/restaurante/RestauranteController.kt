package com.cletaeats.restaurante

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/restaurantes")
class RestauranteController(
    private val restauranteService: RestauranteService
) {

    @GetMapping
    fun listar(
        @RequestParam(required = false, defaultValue = "false")
        soloActivos: Boolean
    ): List<RestauranteResponse> {
        return if (soloActivos) {
            restauranteService.listarActivos()
        } else {
            restauranteService.listarTodos()
        }
    }

    @GetMapping("/{id}")
    fun obtenerPorId(@PathVariable id: Long): RestauranteResponse {
        return restauranteService.obtenerPorId(id)
    }
}