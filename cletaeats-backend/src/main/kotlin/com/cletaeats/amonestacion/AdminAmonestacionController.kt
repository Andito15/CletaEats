package com.cletaeats.amonestacion

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminAmonestacionController(
    private val adminAmonestacionService: AdminAmonestacionService
) {

    @GetMapping("/quejas/{quejaId}/amonestaciones")
    fun listarPorQueja(@PathVariable quejaId: Long): List<AmonestacionAdminResponse> {
        return adminAmonestacionService.listarPorQueja(quejaId)
    }

    @GetMapping("/repartidores/{repartidorId}/amonestaciones")
    fun listarPorRepartidor(@PathVariable repartidorId: Long): List<AmonestacionAdminResponse> {
        return adminAmonestacionService.listarPorRepartidor(repartidorId)
    }
}