package com.cletaeats.combo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ComboController(
    private val comboService: ComboService
) {

    @GetMapping("/restaurantes/{restauranteId}/combos")
    fun listarPorRestaurante(
        @PathVariable restauranteId: Long,
        @RequestParam(required = false, defaultValue = "true")
        soloActivos: Boolean
    ): List<ComboResponse> {
        return if (soloActivos) {
            comboService.listarActivosPorRestaurante(restauranteId)
        } else {
            comboService.listarPorRestaurante(restauranteId)
        }
    }

    @GetMapping("/combos/{comboId}")
    fun obtenerPorId(@PathVariable comboId: Long): ComboResponse {
        return comboService.obtenerPorId(comboId)
    }
}