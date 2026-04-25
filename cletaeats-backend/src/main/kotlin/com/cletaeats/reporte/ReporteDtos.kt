package com.cletaeats.reporte

data class ReporteResumenResponse(
    val usuarios: Long,
    val restaurantes: Long,
    val pedidos: Long,
    val quejas: Long
)

data class ReporteUsuariosDetalleResponse(
    val admins: Long,
    val clientes: Long,
    val repartidores: Long,
    val activos: Long,
    val suspendidos: Long
)

data class ReporteQuejasDetalleResponse(
    val pendientes: Long,
    val enRevision: Long,
    val resueltas: Long,
    val rechazadas: Long
)

data class ReporteConteoResponse(
    val etiqueta: String,
    val total: Long
)

data class ReporteRankingResponse(
    val nombre: String,
    val total: Long
)

data class ReporteRepartidorRankingResponse(
    val nombre: String,
    val pedidosAsignados: Long,
    val amonestacionesActivas: Long
)

data class ReporteDashboardResponse(
    val resumen: ReporteResumenResponse,
    val usuariosDetalle: ReporteUsuariosDetalleResponse,
    val quejasDetalle: ReporteQuejasDetalleResponse,
    val pedidosPorEstado: List<ReporteConteoResponse>,
    val topRestaurantes: List<ReporteRankingResponse>,
    val topRepartidores: List<ReporteRepartidorRankingResponse>
)