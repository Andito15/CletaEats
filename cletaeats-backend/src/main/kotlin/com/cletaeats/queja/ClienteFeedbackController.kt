package com.cletaeats.queja

import com.cletaeats.calificacion.CalificacionRequest
import com.cletaeats.calificacion.CalificacionResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/clientes/pedidos")
class ClienteFeedbackController(
    private val feedbackService: FeedbackService
) {

    @PostMapping("/{pedidoId}/calificacion")
    fun registrarCalificacion(
        principal: Principal,
        @PathVariable pedidoId: Long,
        @Valid @RequestBody request: CalificacionRequest
    ): CalificacionResponse {
        return feedbackService.registrarCalificacion(principal.name, pedidoId, request)
    }

    @PostMapping("/{pedidoId}/queja")
    fun registrarQueja(
        principal: Principal,
        @PathVariable pedidoId: Long,
        @Valid @RequestBody request: QuejaRequest
    ): QuejaResponse {
        return feedbackService.registrarQueja(principal.name, pedidoId, request)
    }
}