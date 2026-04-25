package com.cletaeats.reporte

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/reportes")
class ReporteController(
    private val reporteService: ReporteService
) {

    @GetMapping("/dashboard")
    fun obtenerDashboard(): ReporteDashboardResponse {
        return reporteService.obtenerDashboard()
    }
}