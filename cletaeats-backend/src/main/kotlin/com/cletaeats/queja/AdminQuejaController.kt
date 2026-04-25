package com.cletaeats.queja

import com.cletaeats.amonestacion.AmonestacionRequest
import com.cletaeats.amonestacion.AmonestacionResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/admin/quejas")
class AdminQuejaController(
    private val feedbackService: FeedbackService
) {

    @GetMapping
    fun listarQuejas(): List<QuejaResponse> {
        return feedbackService.listarQuejasAdmin()
    }

    @PostMapping("/{quejaId}/amonestacion")
    fun crearAmonestacion(
        principal: Principal,
        @PathVariable quejaId: Long,
        @Valid @RequestBody request: AmonestacionRequest
    ): AmonestacionResponse {
        return feedbackService.crearAmonestacion(principal.name, quejaId, request)
    }
}