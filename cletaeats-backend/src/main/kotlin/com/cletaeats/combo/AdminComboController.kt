package com.cletaeats.combo

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminComboController(
    private val comboService: ComboService
) {

    @PostMapping("/restaurantes/{restauranteId}/combos")
    fun crear(
        @PathVariable restauranteId: Long,
        @Valid @RequestBody request: ComboRequest
    ): ComboResponse {
        return comboService.crear(restauranteId, request)
    }

    @PutMapping("/combos/{comboId}")
    fun actualizar(
        @PathVariable comboId: Long,
        @Valid @RequestBody request: ComboRequest
    ): ComboResponse {
        return comboService.actualizar(comboId, request)
    }

    @PatchMapping("/combos/{comboId}/estado")
    fun cambiarEstado(
        @PathVariable comboId: Long,
        @Valid @RequestBody request: ComboEstadoRequest
    ): ComboResponse {
        return comboService.cambiarEstado(comboId, request)
    }
}