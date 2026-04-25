package com.cletaeats.reporte

import com.cletaeats.amonestacion.AmonestacionRepartidorRepository
import com.cletaeats.pedido.PedidoRepository
import com.cletaeats.queja.QuejaRepartidorRepository
import com.cletaeats.repartidor.RepartidorRepository
import com.cletaeats.restaurante.RestauranteRepository
import com.cletaeats.usuario.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class ReporteService(
    private val usuarioRepository: UsuarioRepository,
    private val restauranteRepository: RestauranteRepository,
    private val pedidoRepository: PedidoRepository,
    private val repartidorRepository: RepartidorRepository,
    private val amonestacionRepository: AmonestacionRepartidorRepository,
    private val quejaRepartidorRepository: QuejaRepartidorRepository
) {

    fun obtenerDashboard(): ReporteDashboardResponse {
        val usuarios = usuarioRepository.findAll()
        val restaurantes = restauranteRepository.count()
        val pedidos = pedidoRepository.findAllByOrderByFechaPedidoDesc()
        val quejas = quejaRepartidorRepository.findAll()

        val admins = usuarios.count { it.rol?.codigo == "ADMIN" }.toLong()
        val clientes = usuarios.count { it.rol?.codigo == "CLIENTE" }.toLong()
        val repartidores = usuarios.count { it.rol?.codigo == "REPARTIDOR" }.toLong()
        val activos = usuarios.count { it.estado == "ACTIVO" }.toLong()
        val suspendidos = usuarios.count { it.estado == "SUSPENDIDO" }.toLong()

        val pendientes = quejas.count { it.estado == "PENDIENTE" }.toLong()
        val enRevision = quejas.count { it.estado == "EN_REVISION" }.toLong()
        val resueltas = quejas.count { it.estado == "RESUELTA" }.toLong()
        val rechazadas = quejas.count { it.estado == "RECHAZADA" }.toLong()

        val pedidosPorEstado = pedidos
            .groupingBy { it.estado ?: "SIN_ESTADO" }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map {
                ReporteConteoResponse(
                    etiqueta = it.key,
                    total = it.value.toLong()
                )
            }

        val topRestaurantes = pedidos
            .mapNotNull { it.restaurante?.nombre }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map {
                ReporteRankingResponse(
                    nombre = it.key,
                    total = it.value.toLong()
                )
            }

        val topRepartidores = repartidorRepository.findAll()
            .map { repartidor ->
                val nombre = repartidor.usuario?.nombreCompleto ?: "Sin nombre"
                val pedidosAsignados = pedidos.count {
                    it.repartidor?.repartidorId == repartidor.repartidorId
                }.toLong()

                val amonestacionesActivas = repartidor.repartidorId?.let {
                    amonestacionRepository.countByRepartidor_RepartidorIdAndActiva(it, "S")
                } ?: 0L

                ReporteRepartidorRankingResponse(
                    nombre = nombre,
                    pedidosAsignados = pedidosAsignados,
                    amonestacionesActivas = amonestacionesActivas
                )
            }
            .sortedWith(
                compareByDescending<ReporteRepartidorRankingResponse> { it.pedidosAsignados }
                    .thenByDescending { it.amonestacionesActivas }
            )
            .take(5)

        return ReporteDashboardResponse(
            resumen = ReporteResumenResponse(
                usuarios = usuarios.size.toLong(),
                restaurantes = restaurantes,
                pedidos = pedidos.size.toLong(),
                quejas = quejas.size.toLong()
            ),
            usuariosDetalle = ReporteUsuariosDetalleResponse(
                admins = admins,
                clientes = clientes,
                repartidores = repartidores,
                activos = activos,
                suspendidos = suspendidos
            ),
            quejasDetalle = ReporteQuejasDetalleResponse(
                pendientes = pendientes,
                enRevision = enRevision,
                resueltas = resueltas,
                rechazadas = rechazadas
            ),
            pedidosPorEstado = pedidosPorEstado,
            topRestaurantes = topRestaurantes,
            topRepartidores = topRepartidores
        )
    }
}